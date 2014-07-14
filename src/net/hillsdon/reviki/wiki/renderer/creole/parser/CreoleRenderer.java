package net.hillsdon.reviki.wiki.renderer.creole.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.ASTNode;

public class CreoleRenderer {
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    String contents = page.getContent();

    // The grammar and lexer assume they'll not hit an EOF after various things,
    // so add a newline in if there's not one already there.
    if(!contents.substring(contents.length() - 1).equals("\n"))
      contents += "\n";

    ANTLRInputStream in = new ANTLRInputStream(contents);
    CreoleTokens lexer = new CreoleTokens(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    ParseTree tree = parser.creole();

    ParseTreeVisitor<ASTNode> visitor = new Visitor(page, urlOutputFilter, linkHandler, imageHandler);

    return visitor.visit(tree);
  }
}
