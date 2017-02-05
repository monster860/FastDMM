package com.github.monster860.fastdmm.editing.placement;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconSubstate;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class DeletePlacementHandler implements PlacementHandler {
	private Set<Location> usedLocations = new HashSet<>();
	private FastDMM editor;
	private ObjInstance oInstance;
	@Override
	public void init(FastDMM editor, ObjInstance instance, Location initialLocation) {
		this.editor = editor;
		this.oInstance = instance;
		this.usedLocations.add(initialLocation);
	}

	@Override
	public void dragTo(Location location) {
		usedLocations.add(location);
	}

	@Override
	public void finalizePlacement() {
		for(Location l : usedLocations) {
			String key = editor.dmm.map.get(l);
			if(key != null) {
				TileInstance tInstance = editor.dmm.instances.get(key);
				String newKey = tInstance.removeObjectOrSubtypes(oInstance);
				editor.dmm.putMap(l, newKey);
			}
		}
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		for(Location l : usedLocations) {
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l.x;
			ri.y = l.y;
			ri.substate = editor.interface_dmi.getIconState("15").getSubstate(2);
			ri.color = new Color(255,32,16);
			
			rendInstanceSet.add(ri);
		}
		
		return currCreationIndex;
	}

}
