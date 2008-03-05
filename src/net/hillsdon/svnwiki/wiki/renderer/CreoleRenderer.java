package net.hillsdon.svnwiki.wiki.renderer;

// Adapted from the Creole 0.4 implementation in JavaScript available here
// http://www.meatballsociety.org/Creole/0.4/
// Original copyright notice follows:

// Copyright (c) 2007 Chris Purcell.
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//

/**
 * A Creole 0.1 renderer.
 * 
 * @see http://www.wikicreole.org/wiki/Creole1.0
 * @author mth
 */
public class CreoleRenderer {

  private static class Heading extends RuleTreeNode {
    public Heading(final int number) {
      super(String.format("(?:^|\n)={%d}(.+?)(?:\n|$)", number), "h" + number, 1);
    }
  }
  private static class List extends RuleTreeNode {
    public List(final String match, final String tag) {
      super("(^|\\n)(" + match + "[^*#].*(\\n|$)([*#]{2}.*(\\n|$))*)+", tag, 0, "(^|\\n)[*#]", "$1");
    }
  }

  private static final RuleTreeNode ROOT;
  static {
    RuleTreeNode root = new RuleTreeNode("", "", 0);
    RuleTreeNode noWiki = new RuleTreeNode("(?:^|\\n)[{][{][{]\\n?(.*?(\\n.*?)*?)[}][}][}](\\n|$)", "pre", 1);
    RuleTreeNode paragraph = new RuleTreeNode("(?:^|\n)(.+?)(?:$|\n\n)", "p", 1);
    RuleTreeNode italic = new RuleTreeNode("//(.*?)//", "em", 1);
    RuleTreeNode bold = new RuleTreeNode("[*][*](.*?)[*][*]", "strong", 1);
    RuleTreeNode lineBreak = new RuleTreeNode("\\\\", "br", null);
    RuleTreeNode unorderedList = new List("\\*", "ul");
    RuleTreeNode orderedList = new List("#", "ol");
    RuleTreeNode listItem = new RuleTreeNode(".+(\\n[*#].+)*", "li", 0)
                              .setChildren(bold, italic, lineBreak, unorderedList, orderedList);
    root.setChildren(
        noWiki.setChildren(), 
        new Heading(5).setChildren(bold, italic, lineBreak),
        new Heading(4).setChildren(bold, italic, lineBreak), 
        new Heading(3).setChildren(bold, italic, lineBreak), 
        new Heading(2).setChildren(bold, italic, lineBreak), 
        new Heading(1).setChildren(bold, italic, lineBreak),
        orderedList.setChildren(listItem),
        unorderedList.setChildren(listItem),
        paragraph.setChildren(bold, italic, lineBreak, orderedList, unorderedList), 
        italic.setChildren(bold, italic, lineBreak), 
        bold.setChildren(bold, italic, lineBreak));
    ROOT = root;
  }
  
  public String render(final String in) {
    return ROOT.render(in);
  }
  
}