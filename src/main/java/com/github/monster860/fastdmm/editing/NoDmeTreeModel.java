package com.github.monster860.fastdmm.editing;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class NoDmeTreeModel implements TreeModel {

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		return 0;
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		return 0;
	}

	@Override
	public Object getRoot() {
		return this;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		return true;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
	}
	
	@Override
	public String toString() {
		return "No DME Loaded";
	}
}
