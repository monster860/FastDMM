package monster860.fastdmm.editing;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import monster860.fastdmm.FastDMM;
import monster860.fastdmm.dmirender.DMI;
import monster860.fastdmm.dmirender.IconState;
import monster860.fastdmm.dmirender.IconSubstate;
import monster860.fastdmm.dmirender.RenderInstance;
import monster860.fastdmm.dmmmap.Location;
import monster860.fastdmm.dmmmap.TileInstance;
import monster860.fastdmm.objtree.ModifiedType;
import monster860.fastdmm.objtree.ObjInstance;

public class DirectionalPlacementHandler implements PlacementHandler {
	private Location usedLocation;
	private FastDMM editor;
	private ObjInstance oInstance;
	private int dirCount = 1;
	private int usedDir;
	private ObjInstance usedInstance;
	Map<Integer, ObjInstance> dirToInstance = new HashMap<Integer, ObjInstance>();
	@Override
	public void init(FastDMM editor, ObjInstance instance, Location initialLocation) {
		this.editor = editor;
		this.oInstance = instance;
		this.usedInstance = instance;
		this.usedLocation = initialLocation;
		
		DMI dmi = editor.getDmi(oInstance.getIcon(), false);
		if(dmi != null) {
			String iconStateStr = oInstance.getIconState();
			IconState state = dmi.getIconState(iconStateStr);
			dirCount = state.dirCount;
		}
		for(int i = 0; i < dirCount; i++) {
			int dir = IconState.indexToDirArray[i];
			if(oInstance.getDir() == dir) {
				dirToInstance.put(dir, oInstance);
				continue;
			}
			ModifiedType mt = ModifiedType.deriveFrom(oInstance);
			mt.vars.put("dir", "" + dir);
			if(editor.dmm.modifiedTypes.containsKey(mt.toString())) {
				mt = editor.dmm.modifiedTypes.get(mt.toString());
			} else {
				editor.dmm.modifiedTypes.put(mt.toString(), mt);
				if(mt.parent != null) {
					mt.parent.addInstance(mt);
				}
			}
			dirToInstance.put(dir, mt);
		}
	}

	@Override
	public void dragTo(Location location) {
		int dx = location.x - usedLocation.x;
		int dy = location.y - usedLocation.y;
		if(dirCount == 1) {
			usedDir = 2;
		} else if(dirCount == 4) {
			if(dy > Math.abs(dx))
				usedDir = 1;
			else if(dy < -Math.abs(dx))
				usedDir = 2;
			else if(dx > 0)
				usedDir = 4;
			else if(dx < 0)
				usedDir = 8;
			else
				usedDir = oInstance.getDir();
		} else if(dirCount == 8) {
			if(dx == 0 && dy > 0)
				usedDir = 1;
			else if(dx == 0 && dy < 0)
				usedDir = 2;
			else if(dy == 0 && dx > 0)
				usedDir = 4;
			else if(dy == 0 && dx < 0)
				usedDir = 8;
			else if(dx > 0 && dy > 0)
				usedDir = 5;
			else if(dx > 0 && dy < 0)
				usedDir = 6;
			else if(dx < 0 && dy > 0)
				usedDir = 9;
			else if(dx < 0 && dy < 0)
				usedDir = 10;
			else
				usedDir = 2;
		}
		usedInstance = dirToInstance.get(usedDir);
	}

	@Override
	public int visualize(Set<RenderInstance> rendInstanceSet, int currCreationIndex) {
		if(usedInstance == null)
			return currCreationIndex;
		DMI dmi = editor.getDmi(usedInstance.getIcon(), true);
		if(dmi == null)
			return currCreationIndex;
		String iconState = usedInstance.getIconState();
		IconSubstate substate = dmi.getIconState(iconState).getSubstate(usedInstance.getDir());
		
		RenderInstance ri = new RenderInstance(currCreationIndex++);
		ri.layer = usedInstance.getLayer();
		ri.plane = usedInstance.getPlane();
		ri.x = usedLocation.x + (oInstance.getPixelX()/(float)editor.objTree.icon_size);
		ri.y = usedLocation.y + (oInstance.getPixelY()/(float)editor.objTree.icon_size);
		ri.substate = substate;
		ri.color = usedInstance.getColor();
		
		rendInstanceSet.add(ri);
		
		ri = new RenderInstance(currCreationIndex++);
		ri.plane = 101;
		ri.x = usedLocation.x;
		ri.y = usedLocation.y;
		ri.substate = editor.interface_dmi.getIconState("15").getSubstate(2);
		ri.color = new Color(255,255,255);
		
		rendInstanceSet.add(ri);
		
		return currCreationIndex;
	}

	@Override
	public void finalizePlacement() {
		// TODO Auto-generated method stub
		String key = editor.dmm.map.get(usedLocation);
		if(key != null) {
			TileInstance tInstance = editor.dmm.instances.get(key);
			String newKey = tInstance.addObject(usedInstance);
			editor.dmm.putMap(usedLocation, newKey);
		}
	}
}
