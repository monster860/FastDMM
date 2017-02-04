package com.github.monster860.fastdmm.dmmmap;

/**
 * Represents a 3-dimensional position in a DMM file.
 */
public class Location {

    public int x;
    public int y;
    public int z;

    /**
     * Constructs a new Location with the given coordinates
     *
     * @param x The x-coordinate of this new location
     * @param y The y-coordinate of this new location
     * @param z The z-coordinate of this new location
     */
    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Updates the Location with the given coordinates
     *
     * @param x The x-coordinates
     * @param y The y-coordinates
     * @param z The z-coordinates
     */
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Takes a step into the given direction.
     *
     * @param dir The direction to step into.
     * @return The new location.
     */
    public Location getStep(int dir) {
        Location l = new Location(x, y, z);
        if ((dir & 1) != 0)
            l.y++;
        if ((dir & 2) != 0)
            l.y--;
        if ((dir & 4) != 0)
            l.x++;
        if ((dir & 8) != 0)
            l.x--;
        return l;
    }
    
    @Override
    public boolean equals(Object other) {
    	if(!(other instanceof Location))
    		return false;
    	if(other == this)
    		return false;
    	Location o = (Location)other;
    	return x == o.x && y == o.y && z == o.z;
    }
    
    @Override
    public int hashCode() {
    	return (x<<16) + (y<<8) + (z);
    }
}
