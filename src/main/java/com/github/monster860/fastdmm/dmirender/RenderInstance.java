package com.github.monster860.fastdmm.dmirender;

import java.awt.Color;

public class RenderInstance implements Comparable<RenderInstance> {
	public IconSubstate substate;
	public Color color;
	public float x;
	public float y;
	public int plane;
	public float layer;
	public int creationIndex;
	
	public RenderInstance(int creationIndex) {
		this.creationIndex = creationIndex;
	}
	
	@Override
	public int compareTo(RenderInstance o) {
		float cnA = plane;
		float cnB = o.plane;
		if(cnA == cnB) {
			cnA = layer;
			cnB = o.layer;
		}
		if(cnA == cnB) {
			cnA = creationIndex;
			cnB = o.creationIndex;
		}
		return cnA < cnB ? -1 : (cnA == cnB ? 0 : 1);
	}
}
