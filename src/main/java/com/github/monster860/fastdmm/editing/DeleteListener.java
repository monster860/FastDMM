package com.github.monster860.fastdmm.editing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class DeleteListener implements ActionListener {
	
	FastDMM editor;
	Location location;
	ObjInstance oInstance;
	
	public DeleteListener(FastDMM editor, Location mapLocation, ObjInstance instance) {
		this.editor = editor;
		this.location = mapLocation;
		this.oInstance = instance;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(editor.dmm == null)
			return;
		synchronized(editor) {
			String key = editor.dmm.map.get(location);
			if(key == null)
				return;
			TileInstance ti = editor.dmm.instances.get(key);
			if(ti == null)
				return;
			
			String newKey = ti.removeObject(oInstance);
			
			editor.dmm.putMap(location, newKey);
		}
	}
}
