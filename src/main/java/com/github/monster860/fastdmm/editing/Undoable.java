package com.github.monster860.fastdmm.editing;

public interface Undoable {
	public boolean undo();
	public boolean redo();
}
