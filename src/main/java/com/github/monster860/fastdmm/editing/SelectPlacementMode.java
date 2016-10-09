package com.github.monster860.fastdmm.editing;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.IconState;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class SelectPlacementMode implements PlacementMode {
	public FastDMM editor;
	
	public static Set<Location> selection = new HashSet<Location>();
	@Override
	public PlacementHandler getPlacementHandler(FastDMM editor, ObjInstance instance, Location initialLocation) {
		this.editor = editor;
		return new SelectPlacementHandler();
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		if(editor == null)
			return currCreationIndex;
		for(Location l : selection) {
			int dirs = 0;
    		for(int i = 0; i < 4; i++) {
    			int cdir = IconState.indexToDirArray[i];
    			Location l2 = l.getStep(cdir);
    			if(!selection.contains(l2))
    				dirs |= cdir;
    		}
    		if(dirs != 0) {
    			RenderInstance ri = new RenderInstance(currCreationIndex++);
    			ri.plane = 101;
    			ri.x = l.x;
    			ri.y = l.y;
    			ri.substate = editor.interface_dmi.getIconState("" + dirs).getSubstate(2);
    			ri.color = new Color(255,255,255);
    			
    			rendInstanceSet.add(ri);
    		}
		}
		return currCreationIndex;
	}

}
