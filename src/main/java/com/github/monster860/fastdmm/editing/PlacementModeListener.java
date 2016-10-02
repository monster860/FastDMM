package com.github.monster860.fastdmm.editing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.github.monster860.fastdmm.FastDMM;

public class PlacementModeListener implements ActionListener {
	private FastDMM editor;
	private PlacementMode mode;
	
	public PlacementModeListener(FastDMM editor, PlacementMode mode) {
		this.editor = editor;
		this.mode = mode;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		synchronized(editor) {
			editor.placementMode = mode;
		}
	}
}
