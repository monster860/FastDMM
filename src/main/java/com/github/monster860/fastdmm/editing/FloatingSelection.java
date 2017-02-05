package com.github.monster860.fastdmm.editing;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.json.JSONObject;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.Util;
import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconSubstate;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.DMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;
import com.github.monster860.fastdmm.objtree.ObjectTree;

public class FloatingSelection {
	public int x,y,z,width,height;
	Map<Location, TileInstance> objects = new HashMap<Location, TileInstance>();
	public int colorIdx = 0;
	public int colorDir = 1;
	
	public FloatingSelection() { }
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
			
			objects.put(new Location(l.x-x,l.y-y,1), newTi);
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
				map.putMap(l, newKey);
		}
	}
	
	public void toClipboard() {
		JSONObject json = new JSONObject();
		json.put("width", width);
		json.put("height", height);
		for(Entry<Location,TileInstance> entry : objects.entrySet()) {
			Location l = entry.getKey();
			json.put(l.x+","+l.y, entry.getValue().toString());
		}
		StringSelection sel = new StringSelection(json.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
	}
	
	public static FloatingSelection fromClipboard(ObjectTree objTree, DMM map) {
		FloatingSelection floatSel = new FloatingSelection();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			return null;
		String clipboardVal = null;
		try {
			clipboardVal = (String)clipboard.getData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			return null;
		}
		
		JSONObject json;
		try {
			json = new JSONObject(clipboardVal);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if(!json.has("width") && !json.has("height"))
			return null;
		
		floatSel.width = json.getInt("width");
		floatSel.height = json.getInt("height");
		
		for(String key : json.keySet()) {
			Matcher m = Pattern.compile("(\\-?\\d+),(\\-?\\d+)").matcher(key);
			if(m.matches()) {
				Location l = new Location(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),1);
				TileInstance ti = TileInstance.fromString(json.getString(key), objTree, map);
				floatSel.objects.put(l,ti);
			}
		}
		
		return floatSel;
	}
}
