package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.List;

public class UnorderedList extends ASTNode {
  public UnorderedList(List<ASTNode> children) {
    super("ul", children);
  }
}
