package com.github.monster860.fastdmm.dmmmap;

public class Location {
	public int x;
	public int y;
	public int z;
	
	public Location() {
		
	}
	
	public Location(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void set(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof Location)) return false;
		Location otherLocation = (Location) other;
		if((x != otherLocation.x) || (y != otherLocation.y) || (z != otherLocation.z))
			return false;
		return true;
	}
	
	public int hashCode() {
		return (this.z * 256 + this.y) * 256 + this.x;
	}
	
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}
	
	public Location get_step(int dir) {
		Location l = new Location(x, y, z);
		if((dir & 1) != 0)
			l.y++;
		if((dir & 2) != 0)
			l.y--;
		if((dir & 4) != 0)
			l.x++;
		if((dir & 8) != 0)
			l.x--;
		return l;
	}
}
