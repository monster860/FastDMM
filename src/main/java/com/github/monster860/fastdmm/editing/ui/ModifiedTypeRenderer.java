package com.github.monster860.fastdmm.editing.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ModifiedTypeRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 3345856106722737116L;

	ModifiedTypeTableModel model;
	Font defFont;
	Font boldFont;
	Color defaultColor;
	
	public ModifiedTypeRenderer(ModifiedTypeTableModel model) {
		super();
		this.model = model;
		defFont = getFont();
		boldFont = defFont.deriveFont(Font.BOLD);
		defaultColor = getBackground();
	}
	
	public void setValue(Object value) {
		super.setValue(value);
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
		setBackground(defaultColor);
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if(value instanceof BoldString || value instanceof TypeHeaderString)
			cellComponent.setFont(boldFont);
		else
			cellComponent.setFont(defFont);
		
		if(value instanceof TypeHeaderString)
			cellComponent.setBackground(Color.gray);
        
        return cellComponent;
    }
	
	public static class BoldString {
		String s;
		public BoldString(String s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return s;
		}
	}
	public static class TypeHeaderString {
		String s;
		public TypeHeaderString(String s) {
			this.s = s;
		}
		
		@Override
		public String toString() {
			return s;
		}
	}
}
