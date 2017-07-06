package com.github.monster860.fastdmm.editing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.monster860.fastdmm.dmmmap.DMM;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.google.common.collect.BiMap;

public abstract class DMMDiff implements Undoable {
	private DMM dmm;
	private ArrayList<DMMDiff> precedingDiffs = null;
	
	public DMMDiff(DMM dmm){
		this.dmm = dmm;
	}
	
	@Override
	public boolean undo() {
		undoSingle(dmm);
		if(precedingDiffs != null){
			for(int i=0; i<precedingDiffs.size(); i++){
				precedingDiffs.get(i).undo();
			}
		}
		return true;
	}

	@Override
	public boolean redo() {
		if(precedingDiffs != null){
			for(int i=precedingDiffs.size()-1; i>=0; i--){
				precedingDiffs.get(i).redo();
			}
		}
		redoSingle(dmm);
		return true;
	}
	
	public void combineWith(DMMDiff other){
		if(precedingDiffs == null){
			precedingDiffs = new ArrayList<DMMDiff>();
		}
		precedingDiffs.add(other);
	}
	
	protected abstract void undoSingle(DMM dmm);
	protected abstract void redoSingle(DMM dmm);
	
	public static class MapDiff extends DMMDiff {
		private String oldInstance;
		private String newInstance;
		private Location loc;
		
		public MapDiff(DMM dmm, Location loc, String oldInstance, String newInstance){
			super(dmm);
			this.oldInstance = oldInstance;
			this.newInstance = newInstance;
			this.loc = loc;
		}
		
		@Override
		protected void undoSingle(DMM dmm){
			dmm.putMap(loc, oldInstance, false);
		}
		
		@Override
		protected void redoSingle(DMM dmm){
			dmm.putMap(loc, newInstance, false);
		}
	}
	
	public static class InstanceDiff extends DMMDiff {
		private String key;
		private TileInstance ti;
		
		public InstanceDiff(DMM dmm, String key, TileInstance ti) {
			super(dmm);
			this.key = key;
			this.ti = ti;
		}

		@Override
		protected void undoSingle(DMM dmm) {
			dmm.instances.remove(key);
			dmm.unusedKeys.add(key);
		}

		@Override
		protected void redoSingle(DMM dmm) {
			dmm.instances.put(key, ti);
			dmm.unusedKeys.remove(key);
		}
		
	}
	
	public static class ExpandKeysDiff extends DMMDiff {
		private Map<Location, String> oldMap;
		private Map<Location, String> newMap;
		private BiMap<String, TileInstance> oldInstances;
		private BiMap<String, TileInstance> newInstances;
		private List<String> oldUnusedKeys;
		private List<String> newUnusedKeys;
		private int oldKeyLen;
		private int newKeyLen;
		public ExpandKeysDiff(DMM dmm, Map<Location, String> oldMap, Map<Location, String> newMap,
				BiMap<String, TileInstance> oldInstances, BiMap<String, TileInstance> newInstances,
				List<String> oldUnusedKeys, List<String> newUnusedKeys,
				int oldKeyLen, int newKeyLen) {
			super(dmm);
			this.oldMap = oldMap;
			this.newMap = newMap;
			this.oldInstances = oldInstances;
			this.newInstances = newInstances;
			this.oldKeyLen = oldKeyLen;
			this.newKeyLen = newKeyLen;
		}

		@Override
		protected void undoSingle(DMM dmm) {
			dmm.keyLen = oldKeyLen;
			dmm.map = oldMap;
			dmm.instances = oldInstances;
			dmm.unusedKeys = oldUnusedKeys;
		}

		@Override
		protected void redoSingle(DMM dmm) {
			dmm.keyLen = newKeyLen;
			dmm.map = newMap;
			dmm.instances = newInstances;
			dmm.unusedKeys = newUnusedKeys;
		}
		
	}
}
