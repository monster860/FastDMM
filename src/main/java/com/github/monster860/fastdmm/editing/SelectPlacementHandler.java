package com.github.monster860.fastdmm.editing;

import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.Util;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class SelectPlacementHandler implements PlacementHandler {
	
	FastDMM editor;
	Location startLocation;
	Location endLocation;
	
	SelectPlacementMode placementMode;
	
	private int selectMode = SELECT_MODE_REPLACE;
	
	public static final int SELECT_MODE_REPLACE = 0;
	public static final int SELECT_MODE_ADD = 1;
	public static final int SELECT_MODE_SUBTRACT = 2;
	
	@Override
	public void init(FastDMM editor, ObjInstance instance, Location initialLocation) {
		this.editor = editor;
		startLocation = endLocation = initialLocation;
		placementMode = (SelectPlacementMode)editor.placementMode;
		
		if(editor.isCtrlPressed)
			selectMode = SELECT_MODE_SUBTRACT;
		else if(editor.isShiftPressed)
			selectMode = SELECT_MODE_ADD;
	}

	@Override
	public void dragTo(Location location) {
		endLocation = location;
	}

	@Override
	public void finalizePlacement() {
		if(selectMode == SELECT_MODE_REPLACE)
			placementMode.selection.clear();
		Location l1 = new Location(Math.min(startLocation.x, endLocation.x),Math.min(startLocation.y, endLocation.y),Math.min(startLocation.z, endLocation.z));
		Location l2 = new Location(Math.max(startLocation.x, endLocation.x),Math.max(startLocation.y, endLocation.y),Math.max(startLocation.z, endLocation.z));
		for(int x = l1.x; x <= l2.x; x++)
			for(int y = l1.y; y <= l2.y; y++) {
				if(selectMode == SELECT_MODE_SUBTRACT)
					placementMode.selection.remove(new Location(x, y, l1.z));
				else
					placementMode.selection.add(new Location(x, y, l1.z));
			}
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		currCreationIndex = Util.drawBox(editor, rendInstanceSet, currCreationIndex, startLocation, endLocation);
		
		return currCreationIndex;
	}

}
