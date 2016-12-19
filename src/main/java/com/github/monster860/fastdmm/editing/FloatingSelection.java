package com.github.monster860.fastdmm.editing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.Util;
import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconState;
import com.github.monster860.fastdmm.dmirender.IconSubstate;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.DMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class FloatingSelection {
	int x,y,z,width,height;
	Map<Location, TileInstance> objects = new HashMap<Location, TileInstance>();
	int colorIdx = 0;
	int colorDir = 1;
	
	public FloatingSelection(DMM map, Set<Location> selection, FastDMM editor) {
		int minx = 123456;
		int miny = 123456;
		int maxx = -123456;
		int maxy = -123456;
		for(Location l : selection) {
			if(l.x < minx) {
				minx = l.x;
			}
			if(l.y < miny) {
				miny = l.y;
			}
			if(l.x > maxx) {
				maxx = l.x;
			}
			if(l.y > maxy) {
				maxy = l.y;
			}
			z = l.z;
		}
		x = minx;
		y = miny;
		width = maxx-minx+1;
		height = maxy-miny+1;
		System.out.println(x+","+y+","+width+","+height);
		for(Location l : selection) {
			String key = map.map.get(l);
			if(key == null)
				continue;
			TileInstance ti = map.instances.get(key);
			if(ti == null)
				continue;
			
			TileInstance newTi = new TileInstance(new ArrayList<>(), map);
			for(ObjInstance obj : ti.objs) {
				if(editor.inFilter(obj)) {
					newTi.objs.add(obj);
				}
			}
			
			objects.put(new Location(l.x-x,l.y-y,z), newTi);
		}
	}
	
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex, FastDMM editor) {
		colorIdx += colorDir*5;
		if(colorIdx > 255) {
			colorDir = -1;
			colorIdx = 255;
		}
		if(colorIdx < 0) {
			colorDir = 1;
			colorIdx = 0;
		}
		float colorMult = (float)colorIdx / 255;
		for(Entry<Location,TileInstance> entry : objects.entrySet()) {
			Location l = entry.getKey();
			for (ObjInstance oInstance : entry.getValue().getLayerSorted()) {
				if (oInstance == null)
					continue;
				DMI dmi = editor.getDmi(oInstance.getIcon(), true);
				if (dmi == null)
					continue;
				String iconState = oInstance.getIconState();
				IconSubstate substate = dmi.getIconState(iconState).getSubstate(oInstance.getDir());
	
				RenderInstance ri = new RenderInstance(currCreationIndex++);
				ri.layer = oInstance.getLayer();
				ri.plane = oInstance.getPlane();
				ri.x = l.x + x + (oInstance.getPixelX() / (float) editor.objTree.icon_size);
				ri.y = l.y + y + (oInstance.getPixelY() / (float) editor.objTree.icon_size);
				ri.substate = substate;
				Color c = oInstance.getColor();
				ri.color = new Color(c.getRed(), (int)(c.getGreen()*colorMult), (int)(c.getBlue()*colorMult));
	
				rendInstanceSet.add(ri);
			}
		}
		currCreationIndex = Util.drawBox(editor, rendInstanceSet, currCreationIndex, new Location(x,y,z), new Location(x+width-1,y+height-1,z), new Color(255,colorIdx,colorIdx));
		return currCreationIndex;
	}
	public boolean inSelection(Location l) {
		if(l.x < x || l.y < y || l.x >= (x+width) || l.y >= (y+height) || l.z != z)
			return false;
		return true;
	}
	
	public void anchor(DMM map) {
		for(Entry<Location,TileInstance> entry : objects.entrySet()) {
			Location relL = entry.getKey();
			Location l = new Location(x+relL.x,y+relL.y,z);
			String key = map.map.get(l);
			if(key == null)
				continue;
			TileInstance oldTi = map.instances.get(key);
			TileInstance addTi = entry.getValue();
			TileInstance ti = new TileInstance(new ArrayList<>(oldTi.objs), map);
			boolean hasArea = false;
			boolean hasTurf = false;
			for(ObjInstance i : addTi.objs) {
				if(i.istype("/turf")) {
					hasTurf = true;
				} else if(i.istype("/area")) {
					hasArea = true;
				}
			}
			for(Iterator<ObjInstance> iterator = ti.objs.iterator(); iterator.hasNext(); ) {
				ObjInstance i = iterator.next();
				if(hasTurf && i.istype("/turf"))
					iterator.remove();
				else if(hasArea && i.istype("/area"))
					iterator.remove();
			}
			ti.objs.addAll(addTi.objs);
			ti.sortObjs();			
			String newKey = map.getKeyForInstance(ti);
			if(newKey != null)
				map.map.put(l, newKey);
		}
	}
}
