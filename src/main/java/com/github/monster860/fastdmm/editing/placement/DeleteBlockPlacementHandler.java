package com.github.monster860.fastdmm.editing.placement;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.Util;
import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconSubstate;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class DeleteBlockPlacementHandler implements PlacementHandler {
	Location startLocation;
	Location endLocation;
	private FastDMM editor;
	private ObjInstance oInstance;
	@Override
	public void init(FastDMM editor, ObjInstance instance, Location initialLocation) {
		this.editor = editor;
		this.oInstance = instance;
		startLocation = endLocation = initialLocation;
	}

	@Override
	public void dragTo(Location location) {
		endLocation = location;
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		Location l1 = new Location(Math.min(startLocation.x, endLocation.x),Math.min(startLocation.y, endLocation.y),Math.min(startLocation.z, endLocation.z));
		Location l2 = new Location(Math.max(startLocation.x, endLocation.x),Math.max(startLocation.y, endLocation.y),Math.max(startLocation.z, endLocation.z));
		currCreationIndex = Util.drawBox(editor, rendInstanceSet, currCreationIndex, l1, l2, new Color(255, 32, 16));
		
		return currCreationIndex;
	}

	@Override
	public void finalizePlacement() {
		Location l1 = new Location(Math.min(startLocation.x, endLocation.x),Math.min(startLocation.y, endLocation.y),Math.min(startLocation.z, endLocation.z));
		Location l2 = new Location(Math.max(startLocation.x, endLocation.x),Math.max(startLocation.y, endLocation.y),Math.max(startLocation.z, endLocation.z));
		HashSet<Location> locations = new HashSet<Location>();
		for(int x = l1.x; x <= l2.x; x++)
			for(int y = l1.y; y <= l2.y; y++) {
				Location l = new Location(x, y, l1.z);
				
				String key = editor.dmm.map.get(l);
				if(key != null) {
					TileInstance tInstance = editor.dmm.instances.get(key);
					String newKey = tInstance.removeObjectOrSubtypes(oInstance);
					editor.dmm.putMap(l, newKey);
					locations.add(l);
				}
			}
		editor.addToUndoStack(editor.dmm.popDiffs());
	}
}