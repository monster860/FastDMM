package com.github.monster860.fastdmm;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.github.monster860.fastdmm.dmirender.RenderInstance;
import com.github.monster860.fastdmm.dmmmap.Location;

/**
 * This class contains various utilities.
 */
public class Util {

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * Attempts to retrieve a {@link InputStream} representation of a file.
     *
     * @param path a path to a file.
     * @return a {@link InputStream} representation of the file passed
     */
    public static InputStream getFile(String path) {
        return FastDMM.class.getClassLoader().getResourceAsStream(path);
    }

    /**
     * Attempts to retrieve a {@code byte[]} representation of a file.
     *
     * @param path a path to a file.
     * @return a {@code byte[]} representation of a file.
     * @throws IOException
     */
    public static byte[] getFileAsBytes(String path) throws IOException {
        InputStream inputStream = getFile(path);
        if (inputStream != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int read;
            byte[] data = new byte[4096];
            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                output.write(data, 0, read);
            }
            inputStream.close();
            return output.toByteArray();
        }
        return null;
    }

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    public static boolean isWindowsSystem() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }

    /**
     * Converts all separators to the Unix separator of forward slash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    /**
     * Converts all separators to the system separator.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (isWindowsSystem()) {
            return separatorsToWindows(path);
        } else {
            return separatorsToUnix(path);
        }
    }
    
    public static int drawBox(FastDMM editor, Set<RenderInstance> rendInstanceSet, int currCreationIndex, Location a, Location b) {
    	Location l1 = new Location(Math.min(a.x, b.x),Math.min(a.y, b.y),Math.min(a.z, b.z));
		Location l2 = new Location(Math.max(a.x, b.x),Math.max(a.y, b.y),Math.max(a.z, b.z));
		
		if(l1.equals(l2)) {
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l1.x;
			ri.y = l1.y;
			ri.substate = editor.interface_dmi.getIconState("15").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
		} else if(l1.x == l2.x) {
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l1.x;
			ri.y = l1.y;
			ri.substate = editor.interface_dmi.getIconState("14").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l2.x;
			ri.y = l2.y;
			ri.substate = editor.interface_dmi.getIconState("13").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			for(int y = l1.y + 1; y <= l2.y - 1; y++) {
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = l1.x;
				ri.y = y;
				ri.substate = editor.interface_dmi.getIconState("12").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
			}
		} else if(l1.y == l2.y) {
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l1.x;
			ri.y = l1.y;
			ri.substate = editor.interface_dmi.getIconState("11").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l2.x;
			ri.y = l2.y;
			ri.substate = editor.interface_dmi.getIconState("7").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			for(int x = l1.x + 1; x <= l2.x - 1; x++) {
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = x;
				ri.y = l1.y;
				ri.substate = editor.interface_dmi.getIconState("3").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
			}
		} else {
			RenderInstance ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l1.x;
			ri.y = l1.y;
			ri.substate = editor.interface_dmi.getIconState("10").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l2.x;
			ri.y = l2.y;
			ri.substate = editor.interface_dmi.getIconState("5").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l1.x;
			ri.y = l2.y;
			ri.substate = editor.interface_dmi.getIconState("9").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			ri = new RenderInstance(currCreationIndex++);
			ri.plane = 101;
			ri.x = l2.x;
			ri.y = l1.y;
			ri.substate = editor.interface_dmi.getIconState("6").getSubstate(2);
			ri.color = new Color(255,255,255);
			
			rendInstanceSet.add(ri);
			
			for(int x = l1.x + 1; x <= l2.x - 1; x++) {
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = x;
				ri.y = l1.y;
				ri.substate = editor.interface_dmi.getIconState("2").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
				
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = x;
				ri.y = l2.y;
				ri.substate = editor.interface_dmi.getIconState("1").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
			}
			
			for(int y = l1.y + 1; y <= l2.y - 1; y++) {
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = l1.x;
				ri.y = y;
				ri.substate = editor.interface_dmi.getIconState("8").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
				
				ri = new RenderInstance(currCreationIndex++);
				ri.plane = 101;
				ri.x = l2.x;
				ri.y = y;
				ri.substate = editor.interface_dmi.getIconState("4").getSubstate(2);
				ri.color = new Color(255,255,255);
				
				rendInstanceSet.add(ri);
			}
		}
		
    	return currCreationIndex;
    }
}
