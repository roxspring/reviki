package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Render to HTML. This is largely a direct translation of the old
 * ASTNode.toXHTML() style of rendering, with few changes.
 *
 * @author msw
 */
public class HtmlRenderer extends MarkupRenderer<String> {
  /**
   * Most elements have a consistent CSS class. Links and images are an
   * exception (as can be seen in their implementation), as their HTML is
   * generated by a link handler.
   */
  public static final String CSS_CLASS_ATTR = "class='wiki-content'";

  private final PageStore _pageStore;

  private final LinkPartsHandler _linkHandler;

  private final LinkPartsHandler _imageHandler;

  private final Supplier<List<Macro>> _macros;

  public HtmlRenderer(final PageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _pageStore = pageStore;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
    _macros = macros;

    renderer = new HtmlVisitor();
  }

  @Override
  public ASTNode render(final PageInfo page) {
    return CreoleRenderer.render(_pageStore, page, _linkHandler, _imageHandler, _macros);
  }

  @Override
  public String getContentType() {
    return "text/html; charset=utf-8";
  }

  private final class HtmlVisitor extends ASTRenderer<String> {
    public HtmlVisitor() {
      super("");
    }

    @Override
    protected String combine(final String x1, final String x2) {
      return x1 + x2;
    }

    /**
     * Render a node with a tag.
     */
    public String renderTagged(final String tag, final ASTNode node) {
      // Render the children
      String inner = visitASTNode(node);

      // Render the tag
      if (inner.equals("")) {
        return "<" + tag + " " + CSS_CLASS_ATTR + " />";
      }
      else {
        return "<" + tag + " " + CSS_CLASS_ATTR + ">" + inner + "</" + tag + ">";
      }
    }

    /**
     * Render some syntax-highlighted code.
     */
    public String highlight(final String code, final Optional<Languages> language) {
      Renderer highlighter = null;
      if (language.isPresent()) {
        String lang = null;
        switch (language.get()) {
          case CPLUSPLUS:
            lang = XhtmlRendererFactory.CPLUSPLUS;
            break;
          case JAVA:
            lang = XhtmlRendererFactory.JAVA;
            break;
          case XHTML:
            lang = XhtmlRendererFactory.XHTML;
            break;
          case XML:
            lang = XhtmlRendererFactory.XML;
            break;
        }
        highlighter = XhtmlRendererFactory.getRenderer(lang);
      }

      String highlighted = null;
      if (highlighter == null) {
        highlighted = Escape.html(code);
      }
      else {
        try {
          highlighted = highlighter.highlight("", code, "UTF-8", true);
        }
        catch (IOException e) {
          highlighted = Escape.html(code);
        }
      }

      return highlighted.replace("&nbsp;", " ").replace("<br />", "\n");
    }

    @Override
    public String visitBold(final Bold node) {
      return renderTagged("strong", node);
    }

    @Override
    public String visitCode(final Code node) {
      String out = "<pre " + CSS_CLASS_ATTR + ">";
      out += highlight(node.getText(), node.getLanguage());
      out += "</pre>";
      return out;
    }

    @Override
    public String visitHeading(final Heading node) {
      return renderTagged("h" + node.getLevel(), node);
    }

    @Override
    public String visitHorizontalRule(final HorizontalRule node) {
      return renderTagged("hr", node);
    }

    @Override
    public String visitImage(final Image node) {
      LinkPartsHandler handler = node.getHandler();
      PageInfo page = node.getPage();
      LinkParts parts = node.getParts();

      try {
        return handler.handle(page, Escape.html(parts.getText()), parts, urlOutputFilter());
      }
      catch (Exception e) {
        return Escape.html(parts.getText());
      }
    }

    @Override
    public String visitInlineCode(final InlineCode node) {
      String out = "<code " + CSS_CLASS_ATTR + ">";
      out += highlight(node.getText(), node.getLanguage());
      out += "</code>";
      return out;
    }

    @Override
    public String visitItalic(final Italic node) {
      return renderTagged("em", node);
    }

    @Override
    public String visitLinebreak(final Linebreak node) {
      return renderTagged("br", node);
    }

    @Override
    public String visitLink(final Link node) {
      LinkPartsHandler handler = node.getHandler();
      PageInfo page = node.getPage();
      LinkParts parts = node.getParts();

      try {
        return handler.handle(page, Escape.html(parts.getText()), parts, urlOutputFilter());
      }
      catch (Exception e) {
        // Special case: render mailto: as a link if it didn't get interwiki'd
        String target = node.getTarget();
        String title = node.getTitle();
        if (target.startsWith("mailto:")) {
          return String.format("<a href='%s'>%s</a>", target, Escape.html(title));
        }
        else {
          return Escape.html(parts.getText());
        }
      }
    }

    @Override
    public String visitListItem(final ListItem node) {
      return renderTagged("li", node);
    }

    @Override
    public String visitMacroNode(final MacroNode node) {
      String tag = node.isBlock() ? "pre" : "code";
      String inner = Escape.html(node.getText());
      return "<" + tag + " " + CSS_CLASS_ATTR + ">" + inner + "</" + tag + ">";
    }

    @Override
    public String visitOrderedList(final OrderedList node) {
      return renderTagged("ol", node);
    }

    @Override
    public String visitParagraph(final Paragraph node) {
      return renderTagged("p", node);
    }

    @Override
    public String visitStrikethrough(final Strikethrough node) {
      return renderTagged("strike", node);
    }

    @Override
    public String visitTable(final Table node) {
      return renderTagged("table", node);
    }

    @Override
    public String visitTableCell(final TableCell node) {
      if (!isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        return renderTagged("td", node);
      }

      try {
        String out = "<td " + CSS_CLASS_ATTR;
        out += " style='vertical-align:" + unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0) + "'>";
        out += visitASTNode(node);
        out += "</td>";
        return out;
      }
      catch (Exception e) {
        System.err.println("Error when handling directive " + TABLE_ALIGNMENT_DIRECTIVE);
        return renderTagged("td", node);
      }
    }

    @Override
    public String visitTableHeaderCell(final TableHeaderCell node) {
      if (!isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        return renderTagged("th", node);
      }

      try {
        String out = "<th " + CSS_CLASS_ATTR;
        out += " style='vertical-align:" + unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0) + "'>";
        out += visitASTNode(node);
        out += "</th>";
        return out;
      }
      catch (Exception e) {
        System.err.println("Error when handling directive " + TABLE_ALIGNMENT_DIRECTIVE);
        return renderTagged("th", node);
      }
    }

    @Override
    public String visitTableRow(final TableRow node) {
      return renderTagged("tr", node);
    }

    @Override
    public String visitTextNode(final TextNode node) {
      String text = node.getText();
      return node.isEscaped() ? Escape.html(text) : text;
    }

    @Override
    public String visitUnorderedList(final UnorderedList node) {
      return renderTagged("ul", node);
    }
  }
}
