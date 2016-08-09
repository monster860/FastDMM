package com.github.monster860.fastdmm.editing;

import java.awt.Font;

import javax.swing.table.DefaultTableCellRenderer;

public class ModifiedTypeRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 3345856106722737116L;

	ModifiedTypeTableModel model;
	Font defFont;
	Font boldFont;
	
	public ModifiedTypeRenderer(ModifiedTypeTableModel model) {
		super();
		this.model = model;
		defFont = getFont();
		boldFont = defFont.deriveFont(Font.BOLD);
	}
	
	public void setValue(Object value) {
		if(value instanceof BoldString)
			setFont(boldFont);
		else
			setFont(defFont);
		super.setValue(value);
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
}
