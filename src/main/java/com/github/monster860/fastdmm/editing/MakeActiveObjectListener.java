package com.github.monster860.fastdmm.editing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreePath;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.objtree.ModifiedType;
import com.github.monster860.fastdmm.objtree.ObjInstance;
import com.github.monster860.fastdmm.objtree.ObjectTree;

public class MakeActiveObjectListener implements ActionListener {
	FastDMM editor;
	ObjInstance oInstance;
	
	public MakeActiveObjectListener(FastDMM editor, Location mapLocation, ObjInstance instance) {
		this.editor = editor;
		this.oInstance = instance;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(editor.dmm == null)
			return;
		synchronized(editor) {
			editor.selectedObject = oInstance instanceof ObjectTree.Item ? (ObjectTree.Item)oInstance : ((ModifiedType)oInstance).parent;
			editor.selectedInstance = oInstance;
			List<Object> path = new LinkedList<>();
			ObjectTree.Item curr = editor.selectedObject;
			while(curr != null && (curr.istype("/area") || curr.istype("/mob") || curr.istype("/obj") || curr.istype("/turf"))) {
				path.add(0, curr);
				curr = curr.parent;
			}
			path.add(0, editor.objTree);
				
			editor.objTreeVis.setSelectionPath(new TreePath(path.toArray()));
			editor.instancesVis.setModel(editor.selectedObject);
			editor.instancesVis.setSelectedValue(editor.selectedInstance, true);
		}
	}
}
