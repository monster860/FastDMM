package com.github.monster860.fastdmm.editing;

import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public interface PlacementHandler {
	// Called when the mouse button becomes pressed.
	public void init(FastDMM editor, ObjInstance instance, Location initialLocation);
	// Called when mouse moves to a different tile.
	public void dragTo(Location location);
	// Called when the mouse button become depressed.
	public void finalizePlacement();
	// Called every frame when mouse button is pressed to 
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex);
}
