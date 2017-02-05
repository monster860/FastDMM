package com.github.monster860.fastdmm.dmmmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.objtree.ModifiedType;
import com.github.monster860.fastdmm.objtree.ObjectTree;

// DMM loader - Where you will get confused by all the regex.

public class DMM {
	// Parser shit goes here.
	boolean isCommenting = false;
	int parenthesisDepth = 0;
	int stringDepth = 0;
	int stringExpDepth = 0;
	int parenthesesDepth = 0;
	int[] arrayDepth = new int[50];
	
	public FastDMM editor;
	
	public int minX = 1;
	public int minY = 1;
	public int minZ = 1;
	public int maxX = 1;
	public int maxY = 1;
	public int maxZ = 1;
	
	public int keyLen = 0;
	public BiMap<String, TileInstance> instances = HashBiMap.create();
	public Map<Location, String> map = new HashMap<>();
	public List<String> unusedKeys = new ArrayList<>();
	
	public ObjectTree objTree;
	
	public File file;
	boolean isTGM = false;
	
	public String relPath = "";
	
	public float storedViewportX = 0;
	public float storedViewportY = 0;
	public int storedViewportZoom = 32;
	
	public DMM(File file, ObjectTree objTree, FastDMM editor) throws IOException {
		this.file = file;
		this.editor = editor;
		this.objTree = objTree;
		
		Path pathAbsolute = Paths.get(file.getAbsolutePath());
        Path pathBase = Paths.get(new File(objTree.dmePath).getParent());
        Path pathRelative = pathBase.relativize(pathAbsolute);
        relPath = pathRelative.toString();
		
		if(!file.exists()) {
			
			Set<String> unusedKeysSet = new TreeSet<>();
			generateKeys(keyLen, "", unusedKeysSet);
			unusedKeys = new ArrayList<>(unusedKeysSet);
			
			keyLen = 1;
			
			return;
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		String runOn = "";
		Set<String> unusedKeysSet = new HashSet<>();
		
		Map<String, String> substitutions = new TreeMap<>();
		
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(Pattern.matches("//MAP CONVERTED BY dmm2tgm.py THIS HEADER COMMENT PREVENTS RECONVERSION, DO NOT REMOVE", line))
				isTGM = true;
			if(Pattern.matches("\\((\\d*) ?, ?(\\d*) ?, ?(\\d*) ?\\) ?= ?\\{\"", line)) {
				br.reset();
				break;
			}
			br.mark(100);
			line = stripComments(line);
			line = runOn + line;
			if(!line.trim().isEmpty()) {
				if(line.endsWith("\\")) {
					line = line.substring(0, line.length() - 1);
					runOn = line;
				} else if(parenthesisDepth > 0) {
					runOn = line;
				} else {
					runOn = "";
					Matcher m = Pattern.compile("\"([a-zA-Z]*)\" ?= ?\\((.+)\\)").matcher(line);
					if(m.find()) {
						TileInstance ti = TileInstance.fromString(m.group(2), objTree, this);
						
						// Handle cases where DM put in duplicate instances.
						if(instances.inverse().containsKey(ti)) {
							substitutions.put(m.group(1), instances.inverse().get(ti));
							continue;
						}
						instances.put(m.group(1), TileInstance.fromString(m.group(2), objTree, this));
						if(keyLen == 0) {
							keyLen = m.group(1).length();
							// Generate all the instance ID's
							generateKeys(keyLen, "", unusedKeysSet);
						}
						unusedKeysSet.remove(m.group(1));
					}
				}
			}
		}
        unusedKeys.addAll(unusedKeysSet);
		
		Map<Location, String> reverseMap = new HashMap<>();
		
		int partX = -1;
		int partY = -1;
		int partZ = -1;
		int cursorX = 0;
		int cursorY = 0;
		
		while((line = br.readLine()) != null) {
			line = line.trim();
			if(partX == -1) {
				Matcher m = Pattern.compile("\\((\\d*) ?, ?(\\d*) ?, ?(\\d*) ?\\) ?= ?\\{\"").matcher(line);
				if(m.find()) {
					partX = Integer.parseInt(m.group(1));
					partY = Integer.parseInt(m.group(2));
					partZ = Integer.parseInt(m.group(3));
					cursorX = 0;
					cursorY = 0;
				}
				continue;
			}
			if(Pattern.matches("\"}", line)) {
				partX = -1;
				partY = -1;
				partZ = -1;
				continue;
			}
			for(int i = 0; i < line.length(); i += keyLen) {
				Location loc = new Location(cursorX + partX, cursorY + partY, partZ) ;
				String key = line.substring(i, i+keyLen);
				if(substitutions.containsKey(key))
					key = substitutions.get(key);
				reverseMap.put(loc, key);
				
				if(loc.x > maxX) {
					maxX = loc.x;
				}
				if(loc.y > maxY) {
					maxY = loc.y;
				}
				if(loc.z > maxZ) {
					maxZ = loc.z;
				}
				if(loc.x < minX) {
					minX = loc.x;
				}
				if(loc.y < minY) {
					minY = loc.y;
				}
				if(loc.z < minZ) {
					minZ = loc.z;
				}
				
				cursorX++;
			}
			cursorX = 0;
			cursorY += 1;
		}
		
		br.close();
		
		for(Map.Entry<Location, String> entry : reverseMap.entrySet()) {
			putMap(new Location(entry.getKey().x, maxY+minY-entry.getKey().y, entry.getKey().z), entry.getValue());
		}
	}
	
	public void putMap(Location l, String key) {
		if(map.containsKey(l)) {
			TileInstance i = instances.get(map.get(l));
			if (i != null) {
				i.refCount--;
			}
		}
		if(instances.containsKey(key)) {
			TileInstance i = instances.get(key);
			if(i != null)
				i.refCount++;
			map.put(l, key);
		}
	}
	
	public void save() throws FileNotFoundException {
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		PrintStream ps = new PrintStream(file);
		if(isTGM)
			ps.println("//MAP CONVERTED BY dmm2tgm.py THIS HEADER COMMENT PREVENTS RECONVERSION, DO NOT REMOVE "); // Space at the end is intentional
		List<String> instancesList = new ArrayList<>();
		for(Map.Entry<String, TileInstance> ent : instances.entrySet()) {
			if(ent.getValue().refCount <= 0)
				continue;
			instancesList.add("\"" + ent.getKey() + "\" = (" + (isTGM ? ent.getValue().toStringTGM() : ent.getValue().toString()) + ")");
		}
		Collections.sort(instancesList, (a, b) -> reverseCase(a).compareTo(reverseCase(b)));
        instancesList.forEach(ps::println);
		ps.println();
		
		if(!isTGM) {
			// Save normally
			for(int z = minZ; z <= maxZ; z++) {
				ps.println("(1,1," + z + ") = {\"");
				for(int y = maxY; y >= minY; y--) {
					for(int x = minX; x <= maxX; x++) {
						ps.print(map.get(new Location(x, y, z)));
					}
					ps.println();
				}
				ps.println("\"}");
				ps.println();
			}
		} else {
			// Save using TGM
			for(int z = minZ; z <= maxZ; z++) {
				for(int x = minX; x <= maxX; x++) {
					ps.println("(" + x + ",1," + z + ") = {\"");
					for(int y = maxY; y >= minY; y--) {
						ps.println(map.get(new Location(x, y, z)));
					}
					ps.println("\"}");
				}
			}
		}
		ps.close();
	}
	
	public static String reverseCase(String text)
	{
	    char[] chars = text.toCharArray();
	    for (int i = 0; i < chars.length; i++)
	    {
	        char c = chars[i];
	        if (Character.isUpperCase(c))
	        {
	            chars[i] = Character.toLowerCase(c);
	        }
	        else if (Character.isLowerCase(c))
	        {
	            chars[i] = Character.toUpperCase(c);
	        }
	    }
	    return new String(chars);
	}
	
	public Random rand = new Random();
	
	public String getKeyForInstance(TileInstance ti) {
		if(instances.inverse().containsKey(ti)) {
			return instances.inverse().get(ti);
		}
		if(unusedKeys.size() == 0)
			expandKeys();
		if(unusedKeys.size() > 0) {
			// Picking a key randomly reduces chances of merge conflicts, especially if this map editor is used a lot over time.
			// And we all know how much of a pain *those* are.
			String key = unusedKeys.get(rand.nextInt(unusedKeys.size()));
			unusedKeys.remove(key);
			// Assign the instance
			instances.put(key, ti);
			// Return the key
			return key;
		}
		return null;
	}
	
	public void generateKeys(int length, String prefix, Set<String> set) {
		if(length <= 0) {
			set.add(prefix);
			return;
		}
		for(char c = 'a'; c <= 'z'; c++) {
			generateKeys(length - 1, prefix + c, set);
		}
		for(char c = 'A'; c <= 'Z'; c++) {
			generateKeys(length - 1, prefix + c, set);
		}
	}
	
	// All warranties on merge conflicts and diff size are now void if you call this method.
	public void expandKeys() {
		keyLen++;
		Set<String> unusedKeysSet = new TreeSet<>();
		generateKeys(keyLen, "", unusedKeysSet);
		unusedKeys = new ArrayList<>(unusedKeysSet);
		BiMap<String, TileInstance> newInstances = HashBiMap.create();
		Map<Location, String> newMap = new HashMap<>();
		Map<String, String> substitutions = new HashMap<>();
		for(Map.Entry<String, TileInstance> instance : instances.entrySet()) {
			String newKey = unusedKeys.get(rand.nextInt(unusedKeys.size()));
			unusedKeys.remove(newKey);
			substitutions.put(instance.getKey(), newKey);
			newInstances.put(newKey, instance.getValue());
		}
		for(Map.Entry<Location, String> mapInst : map.entrySet()) {
			newMap.put(mapInst.getKey(), substitutions.get(mapInst.getValue()));
		}
		
		instances = newInstances;
		map = newMap;
	}
	
	public void setSize(int nMinX, int nMinY, int nMinZ, int nMaxX, int nMaxY, int nMaxZ) {
		minX = nMinX;
		minY = nMinY;
		minZ = nMinZ;
		maxX = nMaxX;
		maxY = nMaxY;
		maxZ = nMaxZ;
		
		ObjectTree.Item world = objTree.get("/world");
		if(world == null)
			return;
		
		TileInstance ti = TileInstance.fromString(world.getVar("turf") + ", " + world.getVar("area"), objTree, this);
		String defaultInst = getKeyForInstance(ti);
		
		Set<Location> toRemove = new HashSet<>();
		for(Map.Entry<Location, String> mapInst : map.entrySet()) {
			Location l = mapInst.getKey();
			// In range? Don't remove then!
			if(l.x >= minX && l.x <= maxX && l.y >= minY && l.y <= maxY && l.z >= minZ && l.z <= maxZ)
				continue;
			instances.get(mapInst.getValue()).refCount--;
			toRemove.add(mapInst.getKey());
		}
		for(Location l : toRemove) {
			map.remove(l);
		}
		
		for(int x = minX; x <= maxX; x++) {
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					Location l = new Location(x, y, z);
					if(!map.containsKey(l))
						putMap(l, defaultInst);
				}
			}
		}
	}
	
	public String stripComments(String s)
	{
		StringBuilder o = new StringBuilder();
		for(int i = 0; i < s.length(); i++) {
			char pC = ' ';
			if(i - 1 >= 0)
				pC = s.charAt(i - 1);
			char ppC = ' ';
			if(i - 2 >= 0)
				ppC = s.charAt(i - 2);
			char c = s.charAt(i);
			char nC = ' ';
			if(i + 1 < s.length())
				nC = s.charAt(i + 1);
			if(!isCommenting) {
				if(c == '/' && nC == '/' && stringDepth == 0)
					break;
				if(c == '/' && nC == '*' && stringDepth == 0) {
					isCommenting = true;
					continue;
				}
				if(c == '"' && (pC != '\\' || ppC == '\\') && stringDepth != stringExpDepth) {
					stringDepth--;
				} else if(c == '"' && stringDepth == stringExpDepth) {
					stringDepth++;
				}
				if(c == '[' && stringDepth == stringExpDepth)
					arrayDepth[stringExpDepth]++;
				else if(c == '[' && (pC != '\\' || ppC == '\\') && stringDepth != stringExpDepth)
					stringExpDepth++;
				
				if(c == ']' && arrayDepth[stringExpDepth] != 0)
					arrayDepth[stringExpDepth]--;
				else if(c == ']' && stringDepth > 0 && stringDepth == stringExpDepth)
					stringExpDepth--;
				if(c == '(' && stringDepth == stringExpDepth)
					parenthesisDepth++;
				if(c == ')' && stringDepth == stringExpDepth)
					parenthesisDepth--;
				o.append(c);
			}
			else {
				if(c == '*' && nC == '/') {
					isCommenting = false;
					i++;
				}
			}
				
		}
		return o.toString();
	}
}
