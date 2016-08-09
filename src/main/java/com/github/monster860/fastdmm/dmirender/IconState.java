package com.github.monster860.fastdmm.dmirender;

public class IconState {
	public DMI dmi;
	public String name;
	public int dirCount = 1;
	public 	int frameCount = 1;
	public float[] delays;
	public IconSubstate[] substates;

	public IconState(String name) {
		this.name = name;
	}

	public IconSubstate getSubstate(int dir, int frame) {
		return substates[dirToIndex(dir) + ((frame%frameCount)*dirCount)];
	}
	
	public IconSubstate getSubstate(int dir) {
		return getSubstate(dir, 0);
	}
	
	public static final int[] dirToIndexArray = new int[]{0,1,0,1,2,6,4,2,3,7,5,3,2,1,0,0};
	public int dirToIndex(int dir) { // Converts dm dirs (2,1,4,8,6,10,5,9) to indices(0,1,2,3,4,5,6,7)
		if(dirCount == 1) // One direction = don't bother.
			return 0;
		if(dir > 16)
			return 0;
		if(dir < 0)
			return 0;
		int out = dirToIndexArray[dir];
		if(out > 3 && dirCount == 4)
			out -= 4;
		return out;
	}
	
	public static final int[] indexToDirArray = new int[]{2, 1, 4, 8, 6, 10, 5, 9};
}
