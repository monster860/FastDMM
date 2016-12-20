package com.github.monster860.fastdmm.editing;

import java.util.Set;

import javax.swing.JPopupMenu;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public interface PlacementMode {
	// Called to return a placement handler
	public PlacementHandler getPlacementHandler(FastDMM editor, ObjInstance instance, Location initialLocation);
	// Called every frame to add things to the render list - For example, what's currently selected.
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex);
	// Called to add things to a tile's context menu (Not an object's, a tile's)
	public void addToTileMenu(FastDMM editor, Location mapLocation, TileInstance instance, JPopupMenu menu);
	// Called to flush the data
	public void flush(FastDMM editor);
}
