package com.github.monster860.fastdmm.editing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmirender.IconState;
import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;
import com.github.monster860.fastdmm.dmmmap.TileInstance;
import com.github.monster860.fastdmm.objtree.ObjInstance;

public class SelectPlacementMode implements PlacementMode {
    public FastDMM editor;

    public Set<Location> selection = new HashSet<Location>();
    FloatingSelection floatSelect;
    @Override
    public PlacementHandler getPlacementHandler(FastDMM editor, ObjInstance instance, Location initialLocation) {
        this.editor = editor;
        
        if(selection.contains(initialLocation) && !editor.isAltPressed && !editor.isCtrlPressed && !editor.isShiftPressed) {
        	floatSelect = new FloatingSelection(editor.dmm, selection, editor);
			synchronized(editor) {
				for(Location l : selection) {
					String key = editor.dmm.map.get(l);
					if(key == null)
						continue;
					TileInstance ti = editor.dmm.instances.get(key);
					if(ti == null)
						continue;
				
					String newKey = ti.deleteAllInFilter(editor);
				
					editor.dmm.putMap(l, newKey);
				}
			}
			clearSelection();
        }
        
        if(floatSelect != null) {
        	if(floatSelect.inSelection(initialLocation)) {
        		return new MoveFloatingSelectionPlacementHandler();
        	} else {
        		floatSelect.anchor(editor.dmm);
        		floatSelect = null;
        		return null;
        	}
        }
        
        
        return new SelectPlacementHandler();
    }

    @Override
    public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
        if(editor == null)
            return currCreationIndex;
        for(Location l : selection) {
            int dirs = 0;
            for(int i = 0; i < 4; i++) {
                int cdir = IconState.indexToDirArray[i];
                Location l2 = l.getStep(cdir);
                if(!selection.contains(l2))
                    dirs |= cdir;
            }
            if(dirs != 0) {
                RenderInstance ri = new RenderInstance(currCreationIndex++);
                ri.plane = 101;
                ri.x = l.x;
                ri.y = l.y;
                ri.substate = editor.interface_dmi.getIconState("" + dirs).getSubstate(2);
                ri.color = new Color(255,255,255);

                rendInstanceSet.add(ri);
            }
        }
        setCount();
        if(floatSelect != null) {
        	currCreationIndex = floatSelect.visualize(rendInstanceSet, currCreationIndex, editor);
        }
        return currCreationIndex;
    }

    public void setCount() {
        if(selection.size() > 1) {
            editor.statusstring = selection.size() + " tiles selected. ";
        } else if(selection.size() == 1) {
            editor.statusstring = selection.size() + " tile selected. ";
        } else if(selection.size() == 0) {
            editor.statusstring = "No tiles selected. ";
        }
        editor.selection.setText(editor.statusstring);
    }

    public void clearSelection() {
        selection.clear();
    }

	@Override
	public void addToTileMenu(FastDMM editor, Location mapLocation, TileInstance instance, JPopupMenu menu) {
		if(selection.contains(mapLocation)) {
			JMenuItem item = new JMenuItem("Delete in Selection");
			item.addActionListener(new SelectPlacementMode.SelectDeleteListener(editor, this));
			menu.add(item);
		}
	}

	public class SelectDeleteListener implements ActionListener {
		FastDMM editor;
		SelectPlacementMode selection;
		
		public SelectDeleteListener(FastDMM editor, SelectPlacementMode selection) {
			this.editor = editor;
			this.selection = selection;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(editor.dmm == null)
				return;
			synchronized(editor) {
				for(Location l : selection.selection) {
					String key = editor.dmm.map.get(l);
					if(key == null)
						continue;
					TileInstance ti = editor.dmm.instances.get(key);
					if(ti == null)
						continue;
				
					String newKey = ti.deleteAllInFilter(editor);
				
					editor.dmm.putMap(l, newKey);
				}
			}
			selection.clearSelection();
		}
	}
}
