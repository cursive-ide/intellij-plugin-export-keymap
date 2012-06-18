package org.denis.intellij.export.keymap.model

import org.jetbrains.annotations.NotNull

/**
 * @author Denis Zhdanov
 * @since 6/18/12
 */
class ActionData implements DataEntry {

  List<String> id = []
  String shortcut
  String description

  @Override void visit(@NotNull DataVisitor visitor) { visitor.visit(this) }
  @Override String toString() { description }
}
