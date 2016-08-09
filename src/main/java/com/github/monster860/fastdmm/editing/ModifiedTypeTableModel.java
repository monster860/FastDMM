package com.github.monster860.fastdmm.editing;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import com.github.monster860.fastdmm.objtree.ModifiedType;

public class ModifiedTypeTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 3829092639624884439L;
	
	public ModifiedType type;
	public Map<String, String> allVars = new TreeMap<>();
	
	private List<String> displayedKeys = new ArrayList<>();
	private List<Object> displayedVals = new ArrayList<>();
	
	public boolean doReturnTrue = false;
	
	public ModifiedTypeTableModel(ModifiedType mt) {
		type = mt;
		allVars = mt.parent.getAllVars();
		allVars.putAll(mt.vars);
		rebuildTableList();
	}
	
	public void rebuildTableList() {
		displayedKeys.clear();
		displayedVals.clear();
		
		for(Map.Entry<String,String> ent : allVars.entrySet()) {
			if(ent.getKey().equals("type") || ent.getKey().equals("parentType"))
				continue; // These vars are represented in the object tree for the sole purpose of compatibility with byond.
			displayedKeys.add(ent.getKey());
			displayedVals.add(type.vars.containsKey(ent.getKey()) ? new ModifiedTypeRenderer.BoldString(ent.getValue()) : ent.getValue());
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
		return true;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex != 1)
			return;
		String key = displayedKeys.get(rowIndex);
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
