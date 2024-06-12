package com.celano.base.ardrawingsketchandpaint.demo.paint.interfaces;

public interface UndoCommand {
    void undo();
    void redo();
    boolean canUndo();
    boolean canRedo();
}
