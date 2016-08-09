package com.github.monster860.fastdmm.objtree;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JTree;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.DMI;
import com.github.monster860.fastdmm.dmirender.IconSubstate;

public class InstancesRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 931493078348635512L;
	public FastDMM fdmm;
	
	public InstancesRenderer(FastDMM fdmm) {
		this.fdmm = fdmm;
	}
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean sel, boolean hasFocus) {
		super.getListCellRendererComponent(list, value, index, sel, hasFocus);
		if(value instanceof ObjInstance) {
			ObjInstance item = (ObjInstance)value;
			setText(item.getVar("name"));
			setToolTipText(item.toString());
			
			DMI dmi = fdmm.getDmi(item.getIcon(), false);
			if(dmi != null) {
				String iconState = item.getIconState();
				IconSubstate substate = dmi.getIconState(iconState).getSubstate(item.getDir());
				//setSize((int)getPreferredSize().getWidth(), substate.dmi.height);
				//setPreferredSize(getSize());
				setIcon(substate);
			} else {
				setIcon(null);
			}
		}
		
		return this;
	}
}
