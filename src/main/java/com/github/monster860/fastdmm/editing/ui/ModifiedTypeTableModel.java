package com.github.monster860.fastdmm.editing.ui;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import com.github.monster860.fastdmm.objtree.ModifiedType;
import com.github.monster860.fastdmm.objtree.ObjectTree;

public class ModifiedTypeTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3829092639624884439L;
	
	public ModifiedType type;
	
	private List<Object> displayedKeys = new ArrayList<>();
	private List<Object> displayedVals = new ArrayList<>();
	
	public boolean doReturnTrue = false;
	
	public ModifiedTypeTableModel(ModifiedType mt) {
		type = mt;
		rebuildTableList();
	}
	
	public void rebuildTableList() {
		displayedKeys.clear();
		displayedVals.clear();
		
		List<ObjectTree.Item> parentTypes = new ArrayList<>();
		for(ObjectTree.Item currItem = type.parent; currItem != null ;currItem = currItem.parent) {
			parentTypes.add(0, currItem);
		}
		
		Set<String> usedVars = new HashSet<>();
		usedVars.add("type"); // These vars are represented in the object tree for the sole purpose of compatibility with games that modify parentType.
		usedVars.add("parentType");
		
		for(ObjectTree.Item currItem : parentTypes) {
			TreeMap<String, String> sortedVars = new TreeMap<String, String>();
			for(Map.Entry<String,String> ent : currItem.vars.entrySet()) {
				if(usedVars.contains(ent.getKey()))
					continue;
				usedVars.add(ent.getKey());
				sortedVars.put(ent.getKey(), type.getVar(ent.getKey()));
			}
			if(sortedVars.size() == 0)
				continue;
			displayedKeys.add(new ModifiedTypeRenderer.TypeHeaderString(currItem.typeString()));
			displayedVals.add(new ModifiedTypeRenderer.TypeHeaderString(""));
			for(Map.Entry<String,String> ent : sortedVars.entrySet()) {
				displayedKeys.add(ent.getKey());
				displayedVals.add(type.vars.containsKey(ent.getKey()) ? new ModifiedTypeRenderer.BoldString(ent.getValue()) : ent.getValue());
			}
		}
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return displayedKeys.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return displayedKeys.get(rowIndex);
		} else if (columnIndex == 1) {
			return displayedVals.get(rowIndex);
		}
		return null;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex != 1)
			return false;
		if(displayedVals.get(rowIndex) instanceof ModifiedTypeRenderer.TypeHeaderString)
			return false;
		return true;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex != 1)
			return;
		if(displayedVals.get(rowIndex) instanceof ModifiedTypeRenderer.TypeHeaderString)
			return;
		String key = displayedKeys.get(rowIndex).toString();
		type.vars.put(key, value.toString());
		displayedVals.set(rowIndex, new ModifiedTypeRenderer.BoldString(value.toString()));
	}
	
	@Override
	public String getColumnName(int column) {
		if(column == 0)
			return "Key";
		else
			return "Value";
	}
}
