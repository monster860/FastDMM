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

public class DefaultPlacementHandler implements PlacementHandler {
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
		if(!usedLocations.contains(location)) {
			usedLocations.add(location);
		}
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		for(Location l : usedLocations) {
			DMI dmi = editor.getDmi(oInstance.getIcon(), true);
			if(dmi == null)
				continue;
			String iconState = oInstance.getIconState();
			IconSubstate substate = dmi.getIconState(iconState).getSubstate(oInstance.getDir());
			
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.layer = oInstance.getLayer();
			ri.plane = oInstance.getPlane();
			ri.x = l.x + (oInstance.getPixelX()/(float)editor.objTree.icon_size);
			ri.y = l.y + (oInstance.getPixelY()/(float)editor.objTree.icon_size);
			ri.substate = substate;
			ri.color = oInstance.getColor();
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l.x;
			ri.y = l.y;
			ri.substate = editor.interface_dmi.getIconState("15").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
		}
		
		return currCreationIndex;
	}

	@Override
	public void finalizePlacement() {
		for(Location l : usedLocations) {
			String key = editor.dmm.map.get(l);
			if(key != null) {
				TileInstance tInstance = editor.dmm.instances.get(key);
				String newKey = tInstance.addObject(oInstance);
				editor.dmm.putMap(l, newKey);
			}
		}
	}
}
