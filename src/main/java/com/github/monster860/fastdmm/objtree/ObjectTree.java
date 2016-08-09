package com.github.monster860.fastdmm.objtree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class ObjectTree implements TreeModel {
	public HashMap<String,Item> items = new HashMap<>();
	public String dmePath;
	
	public int icon_size;
	
	public ObjectTree()
	{
		// Default datums
		
		Item datum = new Item(null, "/datum");
		datum.setVar("tag","null");
		addItem(datum);
		
		Item atom = new Item(datum, "/atom");
		atom.setVar("alpha", "255");
		atom.setVar("appearance_flags", "0");
		atom.setVar("blend_mode", "0");
		atom.setVar("color", "null");
		atom.setVar("density", "0");
		atom.setVar("desc", "null");
		atom.setVar("dir", "2");
		atom.setVar("gender", "neuter");
		atom.setVar("icon", "null");
		atom.setVar("icon_state", "null");
		atom.setVar("infra_luminosity", "0");
		atom.setVar("invisibility", "0");
		atom.setVar("layer", "1");
		atom.setVar("luminosity", "0");
		atom.setVar("maptext", "null");
		atom.setVar("maptext_width", "32");
		atom.setVar("maptext_height", "32");
		atom.setVar("maptext_x", "0");
		atom.setVar("maptext_y", "0");
		atom.setVar("mouse_drag_pointer", "0");
		atom.setVar("mouse_drop_pointer", "1");
		atom.setVar("mouse_drop_zone", "0");
		atom.setVar("mouse_opacity", "1");
		atom.setVar("mouse_over_pointer", "0");
		atom.setVar("name", "null");
		atom.setVar("opacity", "0");
		atom.setVar("overlays", "list()");
		atom.setVar("override", "0");
		atom.setVar("pixel_x", "0");
		atom.setVar("pixel_y", "0");
		atom.setVar("pixel_z", "0");
		atom.setVar("plane", "0");
		atom.setVar("suffix", "null");
		atom.setVar("transform", "null");
		atom.setVar("underlays", "list()");
		atom.setVar("verbs", "list()");
		addItem(atom);
		
		Item movable = new Item(atom, "/atom/movable");
		movable.setVar("animate_movement", "1");
		movable.setVar("bound_x", "0");
		movable.setVar("bound_y", "0");
		movable.setVar("bound_width", "32");
		movable.setVar("bound_height", "32");
		movable.setVar("glide_size", "0");
		movable.setVar("screen_loc", "null");
		movable.setVar("step_size", "32");
		movable.setVar("step_x", "0");
		movable.setVar("step_y", "0");
		addItem(movable);
		
		Item area = new Item(atom, "/area");
		area.setVar("layer", "1");
		area.setVar("luminosity", "1");
		addItem(area);
		
		Item turf = new Item(atom, "/turf");
		turf.setVar("layer", "2");
		addItem(turf);
		
		Item obj = new Item(movable, "/obj");
		obj.setVar("layer", "3");
		addItem(obj);
		
		Item mob = new Item(movable, "/mob");
		mob.setVar("ckey", "null");
		mob.setVar("density", "1");
		mob.setVar("key", "null");
		mob.setVar("layer", "4");
		mob.setVar("see_in_dark", "2");
		mob.setVar("see_infrared", "0");
		mob.setVar("see_invisible", "0");
		mob.setVar("sight", "0");
		addItem(mob);
		
		Item world = new Item(datum, "/world");
		world.setVar("turf", "/turf");
		world.setVar("mob", "/mob");
		world.setVar("area", "/area");
	}
	
	public Item getOrCreate(String path) {
		if(items.containsKey(path))
			return items.get(path);
		
		String parentPath;
		if(path.indexOf("/") != path.lastIndexOf("/"))
			parentPath = path.substring(0, path.lastIndexOf("/"));
		else
			parentPath = "/datum";
		Item parentItem = getOrCreate(parentPath);
		Item item = new Item(parentItem, path);
		items.put(path, item);
		return item;
	}
	
	public Item get(String path) {
		if(items.containsKey(path))
			return items.get(path);
		else
			return null;
	}
	
	public void addItem(Item item)
	{
		items.put(item.path, item);
	}
	
	public void dumpTree(PrintStream ps)
	{
		for(Item item : items.values())
		{
			ps.println(item.path);
			for(Entry<String, String> var : item.vars.entrySet())
			{
				ps.println("\t" + var.getKey() + " = " + var.getValue());
			}
		}
	}
	
	public Item getGlobal() {
		return items.get("");
	}


	public void completeTree() {
		// Clear children and parse expressions
		
		Item global = getGlobal();
		
		System.gc();
		
		for(Item i : items.values()) {
			i.subtypes.clear();
			
			for(Entry<String, String> e : i.vars.entrySet()) {
				String val = e.getValue();
				String origVal = "";
				try {
				while(!origVal.equals(val)) {
					origVal = val;
					// Trust me, this is the fastest way to parse the macros.
					Matcher m = Pattern.compile("(?<![\\d\\w\"/])\\w+(?![\\d\\w\"/])").matcher(val);
					StringBuffer outVal = new StringBuffer();
					while(m.find()) {
						if(global.vars.containsKey(m.group(0)))
							m.appendReplacement(outVal, global.vars.get(m.group(0)));
						else
							m.appendReplacement(outVal, m.group(0));
					}
					m.appendTail(outVal);
					val = outVal.toString();
					
					// Parse additions/subtractions.
					m = Pattern.compile("([\\d\\.]+)[ \\t]*(\\+|\\-)[ \\t]*([\\d\\.]+)").matcher(val);
					outVal = new StringBuffer();
					while(m.find()) {
						switch(m.group(2)) {
						case "+":
							m.appendReplacement(outVal, (Float.parseFloat(m.group(1)) + Float.parseFloat(m.group(3)))+"");
							break;
						case "-":
							m.appendReplacement(outVal, (Float.parseFloat(m.group(1)) / Float.parseFloat(m.group(3)))+"");
							break;
						}
					}
					m.appendTail(outVal);
					val = outVal.toString();
					
					// Parse parentheses
					m = Pattern.compile("\\(([\\d\\.]+)\\)").matcher(val);
					outVal = new StringBuffer();
					while(m.find()) {
						m.appendReplacement(outVal, m.group(1));
					}
					m.appendTail(outVal);
					val = outVal.toString();
				}
				} catch (OutOfMemoryError ex) {
					System.err.println("OUT OF MEMORY PROCESSING ITEM " + i.typeString() + " VAR " + e.getKey() + " = " + e.getValue());
					throw ex;
				}
				
				i.setVar(e.getKey(), val);
			}
		}
		System.gc();
		// Assign parents/children
		for(Item i : items.values()) {
			Item parent = get(i.getVar("parentType"));
			if(parent != null) {
				i.parent = parent;
				parent.subtypes.add(i);
			}
		}
		System.gc();
		// Sort children
		for(Item i : items.values()) {
			i.subtypes.sort((arg0, arg1) -> arg0.path.compareToIgnoreCase(arg1.path));
		}
		
		try {
			icon_size = Integer.parseInt(get("/world").getVar("icon_size"));
		} catch(NumberFormatException e) {
			icon_size = 32;
		}
	}
	
	public static class Item extends ObjInstance implements ListModel<ObjInstance> {
		public Item(Item parent, String path)
		{
			path = path.trim();
			this.path = path;
			this.parent = parent;
			vars.put("type", path);
			if(parent != null)
			{
				parent.subtypes.add(this);
				vars.put("parentType", parent.path);
			}
			instances.add(this);
		}
		
		public Item(String path)
		{
			this.path = path;
			vars.put("type", path);
			instances.add(this);
		}
		
		public boolean istype(String path) {
			if(this.path == path)
				return true;
			if(parent != null)
				return parent.istype(path);
			return false;
		}
		
		public void setVar(String key, String value)
		{
			vars.put(key, value);
		}
		
		public void setVar(String key)
		{
			if(!vars.containsKey(key))
				vars.put(key, "null");
		}
		
		public String getVar(String key)
		{
			if(vars.containsKey(key))
				return vars.get(key);
			if(parent != null)
				return parent.getVar(key);
			return null;
		}
		
		public Map<String, String> getAllVars() {
			Map<String, String> allVars = new TreeMap<>();
			if(parent != null)
				allVars.putAll(parent.getAllVars());
			allVars.putAll(vars);
			return allVars;
		}
		
		public String path = "";
		public ArrayList<Item> subtypes = new ArrayList<>();
		public Item parent = null;
		public Map<String, String> vars = new TreeMap<>();
		public List<ObjInstance> instances = new ArrayList<>();
		
		public void addInstance(ObjInstance instance) {
			if(instances.contains(instance))
				return;
			instances.add(instance);
			Collections.sort(instances, (o1, o2) -> {
                if(o1 instanceof Item)
                    return -1;
                if(o2 instanceof Item)
                    return 1;
                return o1.toString().compareToIgnoreCase(o2.toString());
            });
			int index = instances.indexOf(instance);
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
			for(ListDataListener l : listeners) {
				l.intervalAdded(event);
			}
		}
		
		public void removeInstance(ObjInstance instance) {
			int index = instances.indexOf(instance);
			if(index == -1)
				return;
			instances.remove(instance);
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
			for(ListDataListener l : listeners) {
				l.intervalRemoved(event);
			}
		}
		
		public String parentlessName() {
			if(path.startsWith(parent.path))
				return path.substring(parent.path.length());
			else
				return path;
		}

		@Override
		public String typeString() {
			return path;
		}
		
		@Override
		public String toString() {
			return path;
		}
		
		@Override
		public String toStringTGM() {
			return path;
		}
		
		private HashSet<ListDataListener> listeners = new HashSet<>();

		@Override
		public void addListDataListener(ListDataListener arg0) {
			listeners.add(arg0);
		}

		@Override
		public ObjInstance getElementAt(int arg0) {
			// TODO Auto-generated method stub
			return instances.get(arg0);
		}

		@Override
		public int getSize() {
			// TODO Auto-generated method stub
			return instances.size();
		}

		@Override
		public void removeListDataListener(ListDataListener arg0) {
			listeners.remove(arg0);
		}

	}

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		// We don't change.
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		if(arg0 == this) {
			switch(arg1) {
			case 0:
				return get("/area");
			case 1:
				return get("/mob");
			case 2:
				return get("/obj");
			case 3:
				return get("/turf");
			}
		} else if (arg0 instanceof Item) {
			Item item = (Item)arg0;
			return item.subtypes.get(arg1);
		}
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		if(arg0 == this)
			return 4;
		if(arg0 instanceof Item) {
			return ((Item)arg0).subtypes.size();
		}
		return 0;
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		if(!(arg1 instanceof Item))
			return 0;
		Item item = (Item)arg1;
		if(arg0 == this) {
			switch(item.path) {
			case "/area":
				return 0;
			case "/mob":
				return 1;
			case "/obj":
				return 2;
			default:
				return 3;
			}
		}
		if(arg0 instanceof Item)
			return ((Item)arg0).subtypes.indexOf(arg1);
		return 0;
	}

	@Override
	public Object getRoot() {
		return this;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		if(arg0 == this)
			return false;
		if(arg0 instanceof Item)
			return ((Item)arg0).subtypes.size() == 0;
		return true;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener arg0) {
		// We don't change
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		// Nope
	}
	
	public String toString() {
		return dmePath;
	}
}
