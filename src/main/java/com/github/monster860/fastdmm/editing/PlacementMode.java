package com.github.monster860.fastdmm.editing;

import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public interface PlacementMode {
	// Called to return a placement handler
	public PlacementHandler getPlacementHandler(FastDMM editor, ObjInstance instance, Location initialLocation);
	// Called every frame to add things to the render list - For example, what's currently selected.
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex);
}
