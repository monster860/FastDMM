package com.github.monster860.fastdmm.editing.placement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.editing.Undoable;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public abstract class UndoablePlacement implements Undoable{
	protected FastDMM editor;
	protected Set<Location> locations;
	
	public UndoablePlacement(FastDMM editor, Set<Location> locations){
		this.editor = editor;
		this.locations = locations;
	}
	
	public UndoablePlacement(FastDMM editor, Location location){
		this.editor = editor;
		this.locations = new HashSet<Location>();
		locations.add(location);
	}

	@Override
	public boolean undo() {
		if(editor.dmm == null)
			return false;
		for(Location l : locations) {
			if(editor.dmm.map.get(l) != null){
				undoLoc(l);
			}
		}
		return true;
	}

	@Override
	public boolean redo() {
		if(editor.dmm == null)
			return false;
		for(Location l : locations) {
			if(editor.dmm.map.get(l) != null){
				redoLoc(l);
			}
		}
		return true;
	}
	
	public abstract void undoLoc(Location l);
	public abstract void redoLoc(Location l);
	
	public static class Add extends UndoablePlacement{
		private ObjInstance instance;

		public Add(FastDMM editor, ObjInstance instance, Set<Location> locations) {
			super(editor, locations);
			this.instance = instance;
		}
		
		public Add(FastDMM editor, ObjInstance instance, Location location) {
			super(editor, location);
			this.instance = instance;
		}

		@Override
		public void undoLoc(Location l) {
			String key = editor.dmm.map.get(l);
			TileInstance tInstance = editor.dmm.instances.get(key);
			String newKey = tInstance.removeObjectOrSubtypes(instance);
			editor.dmm.putMap(l, newKey);
		}

		@Override
		public void redoLoc(Location l) {
			String key = editor.dmm.map.get(l);
			TileInstance tInstance = editor.dmm.instances.get(key);
			String newKey = tInstance.addObject(instance);
			editor.dmm.putMap(l, newKey);
		}
		
	}
	
	public static class Delete extends UndoablePlacement{
		private ObjInstance instance;

		public Delete(FastDMM editor, ObjInstance instance, Set<Location> locations) {
			super(editor, locations);
			this.instance = instance;
		}

		public Delete(FastDMM editor, ObjInstance instance, Location location) {
			super(editor, location);
			this.instance = instance;
		}

		@Override
		public void undoLoc(Location l) {
			String key = editor.dmm.map.get(l);
			TileInstance tInstance = editor.dmm.instances.get(key);
			String newKey = tInstance.addObject(instance);
			editor.dmm.putMap(l, newKey);
		}

		@Override
		public void redoLoc(Location l) {
			String key = editor.dmm.map.get(l);
			TileInstance tInstance = editor.dmm.instances.get(key);
			String newKey = tInstance.removeObjectOrSubtypes(instance);
			editor.dmm.putMap(l, newKey);
		}
	}
	
	public static class Replace extends UndoablePlacement{
		private String preKey;
		private String postKey;

		public Replace(FastDMM editor, String preKey, String postKey, Set<Location> locations) {
			super(editor, locations);
			this.preKey = preKey;
			this.postKey = postKey;
		}
		
		public Replace(FastDMM editor, String preKey, String postKey, Location location) {
			super(editor, location);
			this.preKey = preKey;
			this.postKey = postKey;
		}

		@Override
		public void undoLoc(Location l) {
			editor.dmm.putMap(l, preKey);
		}

		@Override
		public void redoLoc(Location l) {
			editor.dmm.putMap(l, postKey);
		}
	}
	
	public static class Move extends UndoablePlacement{
		private Map<Location, String[]> loc_keys;
		
		//The zeroth entry in the String[] is the key before the change, the first entry is the key after the change
		public Move(FastDMM editor, Map<Location, String[]> locations) {
			super(editor, locations.keySet());
			loc_keys = locations;
		}
		
		@Override
		public void undoLoc(Location l) {
			String newKey = loc_keys.get(l)[0];
			editor.dmm.putMap(l, newKey);
		}
		
		@Override
		public void redoLoc(Location l) {
			String newKey = loc_keys.get(l)[1];
			editor.dmm.putMap(l, newKey);
		}
	}
	
}
