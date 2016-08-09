package monster860.fastdmm.dmmmap;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.*;

import monster860.fastdmm.dmirender.DMI;
import monster860.fastdmm.dmirender.IconSubstate;
import monster860.fastdmm.objtree.ModifiedType;
import monster860.fastdmm.objtree.ObjInstance;
import monster860.fastdmm.objtree.ObjectTree;

public class TileInstance {
	public List<ObjInstance> objs;
	DMM dmm;
	public int refCount = 0;
	
	public TileInstance(List<ObjInstance> objs, DMM dmm) {
		this.dmm = dmm;
		this.objs = objs;
	}
	
	public static final TileInstance fromString(String s, ObjectTree objTree, DMM dmm) {
		// This regex matches modified types: /blah/blah{a = "b"; c = 23}
		try {
			Matcher m = Pattern.compile("[\\w/]+(?:\\{(?:\"(?:\\\\\"|[^\"])*?\"|[^\\}])*?\\})?(?=,|$)").matcher(s);
			List<ObjInstance> objs = new ArrayList<ObjInstance>();
			while(m.find()) {
				objs.add(ModifiedType.fromString(m.group(0), objTree, dmm));
			}
			return new TileInstance(objs, dmm);
		} catch(StackOverflowError e) {
			System.err.println(s);
			return null;
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for(ObjInstance obj : objs) {
			if(isFirst)
				isFirst = false;
			else
				sb.append(",");
			sb.append(obj);
		}
		return sb.toString();
	}
	
	public String toStringTGM() {
		StringBuilder sb = new StringBuilder("\n");
		boolean isFirst = true;
		for(ObjInstance obj : objs) {
			if(isFirst)
				isFirst = false;
			else
				sb.append(",\n");
			sb.append(obj.toStringTGM());
		}
		return sb.toString();
	}
	private List<ObjInstance> cachedSorted;
	
	public List<ObjInstance> getLayerSorted() {
		if(cachedSorted == null) {
			cachedSorted = new ArrayList<ObjInstance>(objs);
			cachedSorted.sort(new Comparator<ObjInstance>(){
				@Override
				public int compare(ObjInstance a, ObjInstance b) {
					try {
						float layerA = Float.parseFloat(a.getVar("plane"));
						float layerB = Float.parseFloat(b.getVar("plane"));
						if(layerA == layerB) {
							layerA = Float.parseFloat(a.getVar("layer"));
							layerB = Float.parseFloat(b.getVar("layer"));
						}
						if(layerA == layerB) {
							// Sort by type
							if(a.typeString().startsWith("/turf"))
								layerA = 1;
							if(a.typeString().startsWith("/obj"))
								layerA = 2;
							if(a.typeString().startsWith("/mob"))
								layerA = 3;
							if(a.typeString().startsWith("/area"))
								layerA = 4;
							if(b.typeString().startsWith("/turf"))
								layerB = 1;
							if(b.typeString().startsWith("/obj"))
								layerB = 2;
							if(b.typeString().startsWith("/mob"))
								layerB = 3;
							if(b.typeString().startsWith("/area"))
								layerB = 4;
						}
						return (int)Math.signum(layerA - layerB);
					} catch (Exception e) {
						if(a != null && b != null) {
							System.out.println("Error with layers " + a.getVar("layer") + ", " + b.getVar("layer"));
						}
						return 0;
					}
				}
			});
		}
		return cachedSorted;
	}
	
	private ObjInstance cachedArea = null;
	public ObjInstance getArea() {
		if(cachedArea == null) {
			for(ObjInstance i : objs) {
				if(i == null)
					continue;
				if(i.istype("/area"))
					cachedArea = i;
			}
		}
		return cachedArea;
	}
	
	public void sortObjs() {
		// Sort the object list in the order 
		Collections.sort(objs, new Comparator<ObjInstance>() {
			@Override
			public int compare(ObjInstance a, ObjInstance b) {
				int iA = 0;
				int iB = 0;
				if(a.istype("/obj"))
					iA = 1;
				else if(a.istype("/mob"))
					iA = 2;
				else if(a.istype("/turf"))
					iA = 3;
				else if(a.istype("/area"))
					iA = 4;
				if(b.istype("/obj"))
					iB = 1;
				else if(b.istype("/mob"))
					iB = 2;
				else if(b.istype("/turf"))
					iB = 3;
				else if(b.istype("/area"))
					iB = 4;
				return iA < iB ? -1 : (iA == iB ? 0 : 1);
			}
		});
	}
	
	// Modification functions. They do not modify the tile instance, they return the key pointing to the modified instance.
	public String addObject(ObjInstance obj) {
		TileInstance ti = new TileInstance(new ArrayList<ObjInstance>(objs), dmm);
		if(obj.istype("/area")) {
			for(int i = 0; i < ti.objs.size(); i++) {
				ObjInstance cobj = ti.objs.get(i);
				if(cobj.istype("/area"))
					ti.objs.remove(i);
			}
		}
		if(obj.istype("/turf")) {
			DMI dmi = dmm.editor.getDmi(obj.getIcon(), false);
			if(dmi != null) {
				String iconState = obj.getIconState();
				IconSubstate substate = dmi.getIconState(iconState).getSubstate(obj.getDir());
				if(substate.isOpaque()) {
					for(int i = 0; i < ti.objs.size(); i++) {
						ObjInstance cobj = ti.objs.get(i);
						if(cobj.istype("/turf"))
							ti.objs.remove(i);
					}
				}
			}
		}
		ti.objs.add(obj);
		ti.sortObjs();
		return dmm.getKeyForInstance(ti);
	}
	
	// Removes the bottom-most occurence of the obj.
	public String removeObject(ObjInstance obj) {
		TileInstance ti = new TileInstance(new ArrayList<ObjInstance>(objs), dmm);
		ObjectTree.Item replacement = null;
		if(obj.istype("/area"))
			replacement = dmm.objTree.get(dmm.objTree.get("/world").getVar("area"));
		else if(obj.istype("/turf"))
			replacement = dmm.objTree.get(dmm.objTree.get("/world").getVar("turf"));
		if(replacement != null)
			ti.objs.set(ti.objs.indexOf(obj), replacement);
		else
			ti.objs.remove(obj);
		return dmm.getKeyForInstance(ti);
	}
	
	public String moveObjToTop(ObjInstance obj) {
		TileInstance ti = new TileInstance(new ArrayList<ObjInstance>(objs), dmm);
		ti.objs.remove(obj);
		ti.objs.add(obj);
		ti.sortObjs();
		return dmm.getKeyForInstance(ti);
	}
	
	public String moveObjToBottom(ObjInstance obj) {
		TileInstance ti = new TileInstance(new ArrayList<ObjInstance>(objs), dmm);
		ti.objs.remove(obj);
		ti.objs.add(0, obj);
		ti.sortObjs();
		return dmm.getKeyForInstance(ti);
	}
	
	// Replaces the bottom-most occurence of the obj. 
	public String replaceObject(ObjInstance objA, ObjInstance objB) {
		TileInstance ti = new TileInstance(new ArrayList<ObjInstance>(objs), dmm);
		ti.objs.set(ti.objs.indexOf(objA), objB);
		return dmm.getKeyForInstance(ti);
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof TileInstance))
			return false;
		if(other == this)
			return true;
		if(other.toString().equals(toString()))
			return true;
		return false;
	}
}
