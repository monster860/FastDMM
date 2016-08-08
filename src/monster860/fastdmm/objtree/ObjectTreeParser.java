package monster860.fastdmm.objtree;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

// Ah, the parser. It looks like it was written by 2 people: One that knows regex, and one that doesn't.
// Part of it was written before I had any clue how to regex, and the other part was after.

// I'm still amazed that this runs faster than BYOND's object tree generator, despite it being written in Java.

public class ObjectTreeParser {
	boolean isCommenting = false;
	boolean inMultilineString = false;
	int multilineStringDepth = 0;
	int parenthesisDepth = 0;
	int stringDepth = 0;
	int stringExpDepth = 0;
	int parenthesesDepth = 0; 
	
	public ObjectTree tree;
	
	public Map<String, String> macros = new HashMap<String, String>();
	public JFrame modalParent;
	
	public ObjectTreeParser() {
		tree = new ObjectTree();
		initializeMacros();
	}
	public ObjectTreeParser(ObjectTree tree) {
		this.tree = tree;
		initializeMacros();
	}
	
	private void initializeMacros() {
		// Basically, a bunch of shit pulled from stddef.dm
		
		// directions
		macros.put("NORTH", "1");
		macros.put("SOUTH", "2");
		macros.put("EAST", "4");
		macros.put("WEST", "8");
		macros.put("NORTHEAST", "5");
		macros.put("NORTHWEST", "9");
		macros.put("SOUTHEAST", "6");
		macros.put("SOUTHWEST", "10");
		macros.put("UP", "16");
		macros.put("DOWN", "32");
		// eye and sight
		macros.put("BLIND", "1");
		macros.put("SEE_MOBS", "4");
		macros.put("SEE_OBJS", "8");
		macros.put("SEE_TURFS", "16");
		macros.put("SEE_SELF", "32");
		macros.put("SEE_INFRA", "64");
		macros.put("SEE_PIXELS", "256");
		macros.put("SEEINVIS", "2");
		macros.put("SEEMOBS", "4");
		macros.put("SEEOBJS", "8");
		macros.put("SEETURFS", "16");
		macros.put("MOB_PERSPECTIVE", "0");
		macros.put("EYE_PERSPECTIVE", "1");
		macros.put("EDGE_PERSPECTIVE", "2");
		// layers
		macros.put("FLOAT_LAYER", "-1");
		macros.put("AREA_LAYER", "1");
		macros.put("TURF_LAYER", "2");
		macros.put("OBJ_LAYER", "3");
		macros.put("MOB_LAYER", "4");
		macros.put("FLY_LAYER", "5");
		macros.put("EFFECTS_LAYER", "5000");
		macros.put("TOPDOWN_LAYER", "10000");
		macros.put("BACKGROUND_LAYER", "20000");
		macros.put("FLOAT_PLANE", "-32767");
		// map formats
		macros.put("TOPDOWN_MAP", "0");
		macros.put("ISOMETRIC_MAP", "1");
		macros.put("SIDE_MAP", "2");
		macros.put("TILED_ICON_MAP", "32768");
		// gliding
		macros.put("NO_STEPS", "0");
		macros.put("FORWARD_STEPS", "1");
		macros.put("SLIDE_STEPS", "2");
		macros.put("SYNC_STEPS", "3");
		// appearance_flags
		macros.put("LONG_GLIDE", "1");
		macros.put("RESET_COLOR", "2");
		macros.put("RESET_ALPHA", "4");
		macros.put("RESET_TRANSFORM", "8");
		macros.put("NO_CLIENT_COLOR", "16");
		macros.put("KEEP_TOGETHER", "32");
		macros.put("KEEP_APART", "64");
		macros.put("PLANE_MASTER", "128");
		macros.put("TILE_BOUND", "256");
		macros.put("TRUE", "1");
		macros.put("FALSE", "0");
		macros.put("MALE", "\"male\"");
		macros.put("FEMALE", "\"female\"");
		macros.put("NEUTER", "\"neuter\"");
		macros.put("PLURAL", "\"plural\"");
		macros.put("MOUSE_INACTIVE_POINTER", "0");
		macros.put("MOUSE_ACTIVE_POINTER", "1");
		macros.put("MOUSE_DRAG_POINTER", "3");
		macros.put("MOUSE_DROP_POINTER", "4");
		macros.put("MOUSE_ARROW_POINTER", "5");
		macros.put("MOUSE_CROSSHAIRS_POINTER", "6");
		macros.put("MOUSE_HAND_POINTER", "7");
		macros.put("MOUSE_LEFT_BUTTON", "1");
		macros.put("MOUSE_RIGHT_BUTTON", "2");
		macros.put("MOUSE_MIDDLE_BUTTON", "4");
		macros.put("MOUSE_CTRL_KEY", "8");
		macros.put("MOUSE_SHIFT_KEY", "16");
		macros.put("MOUSE_ALT_KEY", "32");
		macros.put("CONTROL_FREAK_ALL", "1");
		macros.put("CONTROL_FREAK_SKIN", "2");
		macros.put("CONTROL_FREAK_MACROS", "4");
		macros.put("MS_WINDOWS", "\"MS Windows\"");
		macros.put("UNIX", "\"UNIX\"");
		macros.put("_DM_datum", "0x001");
		macros.put("_DM_atom", "0x002");
		macros.put("_DM_movable", "0x004");
		macros.put("_DM_sound", "0x020");
		macros.put("_DM_Icon", "0x100");
		macros.put("_DM_RscFile", "0x200");
		macros.put("_DM_Matrix", "0x400");
		macros.put("_DM_Database", "0x1000");
		macros.put("_DM_Regex", "0x2000");

		// sound
		macros.put("SOUND_MUTE", "1");
		macros.put("SOUND_PAUSED", "2");
		macros.put("SOUND_STREAM", "4");
		macros.put("SOUND_UPDATE", "16");

		// icons
		macros.put("ICON_ADD", "0");
		macros.put("ICON_SUBTRACT", "1");
		macros.put("ICON_MULTIPLY", "2");
		macros.put("ICON_OVERLAY", "3");
		macros.put("ICON_AND", "4");
		macros.put("ICON_OR", "5");
		macros.put("ICON_UNDERLAY", "6");

		// matrix
		macros.put("MATRIX_COPY", "0");
		macros.put("MATRIX_MULTIPLY", "1");
		macros.put("MATRIX_ADD", "2");
		macros.put("MATRIX_SUBTRACT", "3");
		macros.put("MATRIX_INVERT", "4");
		macros.put("MATRIX_ROTATE", "5");
		macros.put("MATRIX_SCALE", "6");
		macros.put("MATRIX_TRANSLATE", "7");
		macros.put("MATRIX_INTERPOLATE", "8");
		macros.put("MATRIX_MODIFY", "128");

		// animation easing
		macros.put("LINEAR_EASING", "0");
		macros.put("SINE_EASING", "1");
		macros.put("CIRCULAR_EASING", "2");
		macros.put("CUBIC_EASING", "3");
		macros.put("BOUNCE_EASING", "4");
		macros.put("ELASTIC_EASING", "5");
		macros.put("BACK_EASING", "6");
		macros.put("QUAD_EASING", "7");
		macros.put("EASE_IN", "64");
		macros.put("EASE_OUT", "128");

		// animation flags
		macros.put("ANIMATION_END_NOW", "1");
		macros.put("ANIMATION_LINEAR_TRANSFORM", "2");

		// blend_mode
		macros.put("BLEND_DEFAULT", "0");
		macros.put("BLEND_OVERLAY", "1");
		macros.put("BLEND_ADD", "2");
		macros.put("BLEND_SUBTRACT", "3");
		macros.put("BLEND_MULTIPLY", "4");

		// Database
		macros.put("DATABASE_OPEN", "0");
		macros.put("DATABASE_CLOSE", "1");
		macros.put("DATABASE_ERROR_CODE", "2");
		macros.put("DATABASE_ERROR", "3");
		macros.put("DATABASE_QUERY_CLEAR", "4");
		macros.put("DATABASE_QUERY_ADD", "5");
		macros.put("DATABASE_QUERY_EXEC", "8");
		macros.put("DATABASE_QUERY_NEXT", "9");
		macros.put("DATABASE_QUERY_ABORT", "10");
		macros.put("DATABASE_QUERY_RESET", "11");
		macros.put("DATABASE_QUERY_ROWS_AFFECTED", "12");
		macros.put("DATABASE_ROW_COLUMN_NAMES", "16");
		macros.put("DATABASE_ROW_COLUMN_VALUE", "17");
		macros.put("DATABASE_ROW_LIST", "18");
	}
	
	public void doParse(File file, boolean isMainFile) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		ArrayList<String> lines = new ArrayList<String>();
		StringBuilder runOn = new StringBuilder();;
		int includeCount = 0;
		// This part turns spaces into tabs, strips all the comments, and puts multiline statements on one line.
		while ((line = br.readLine()) != null) {
			line = stripComments(line);
			line = line.replaceAll("\\t", " ");
			if(!line.trim().isEmpty()) {
				if(line.endsWith("\\")) {
					line = line.substring(0, line.length() - 1);
					runOn.append(line);
				} else if(inMultilineString) {
					runOn.append(line);
					runOn.append("\\n");
				} else if(parenthesisDepth > 0) {
					runOn.append(line);
				} else {
					runOn.append(line);
					line = runOn.toString();
					runOn.setLength(0);
					lines.add(line);
					if(isMainFile && line.trim().startsWith("#include"))
						includeCount++;
				}
			}
		}
		br.close();
		
		ArrayList<String> pathTree = new ArrayList<String>();
		
		int currentInclude = 0;
		
		JProgressBar dpb = null;
		JDialog dlg = null;
		JLabel lbl = null;
		if(isMainFile) {
			final JDialog tdlg = new JDialog(modalParent, "Object Tree Generation", modalParent == null ? false : true);
			dlg = tdlg;
			dpb = new JProgressBar(0, includeCount);
			dlg.add(BorderLayout.CENTER, dpb);
			lbl = new JLabel("");
			dlg.add(BorderLayout.NORTH, lbl);
			dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dlg.setSize(300, 75);
			Thread t = new Thread(new Runnable() {
				public void run() {
					tdlg.setVisible(true);
				}
			});
			t.start();
		}
		
		for(int i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			// Process #include, #define, and #undef
			if(line.trim().startsWith("#")) {
				line = line.trim();
				if(line.startsWith("#include")) {
					String path = line.split("\"")[1];
					if(isMainFile) {
						lbl.setText(path);
					}
					if(path.endsWith(".dm") || path.endsWith(".dme")) {
						File includeFile = new File(file.getParentFile(), path);
						ObjectTreeParser parser = new ObjectTreeParser(tree);
						parser.macros = macros;
						parser.doParse(includeFile, false);
					}
					if(isMainFile) {
						currentInclude++;
						//System.out.println("Obj tree progress: " + (currentInclude*100f)/includeCount);
						dpb.setValue(currentInclude);
					}
				}
				if(line.startsWith("#define")) {
					Matcher m = Pattern.compile("#define +([\\d\\w]+) +(.+)").matcher(line);
					if(m.find()) {
						macros.put(m.group(1), m.group(2).replace("$", "\\$"));
					}
				}
				if(line.startsWith("#undef")) {
					Matcher m = Pattern.compile("#undef[ \\t]*([\\d\\w]+)").matcher(line);
					if(m.find() && macros.containsKey(m.group(1))) {
						macros.remove(m.group(1));
					}
				}
				
				continue;
			}
			// How far is this line indented?
			int level = 0;
			for(int j = 0; j < line.length(); j++) {
				if(line.charAt(j) == ' ')
					level++;
				else
					break;
			}
			// Rebuild the path tree.
			for(int j = pathTree.size(); j <= level; j++)
				pathTree.add("");
			pathTree.set(level, cleanPath(line.trim()));
			if(pathTree.size() > level + 1)
				for(int j = pathTree.size() - 1; j > level; j--)
					pathTree.remove(j);
			String fullPath = "";
			for(String c : pathTree)
				fullPath += c;
			// Now, split it again, and rebuild it again, but only figure out how big the object itself is.
			String[] divided = fullPath.split("\\/");
			String affectedObjectPath = "";
			for(int j = 0; j < divided.length; j++)
			{
				String item = divided[j];
				if(item.isEmpty()) {
					continue;
				}
				if(item.equalsIgnoreCase("static") || item.equalsIgnoreCase("global") || item.equalsIgnoreCase("tmp"))
					continue;
				if(item.equals("proc") || item.equals("verb") || item.equals("var")) {
					break;
				}
				if(item.contains("=") || item.contains("(")) {
					break;
				}
				affectedObjectPath += "/" + item;
			}
			ObjectTree.Item item = tree.getOrCreate(affectedObjectPath);
			if(fullPath.contains("(") && fullPath.indexOf("(") < fullPath.lastIndexOf("/"))
				continue;
			fullPath = fullPath.replaceAll("/tmp", ""); // Let's avoid giving a shit about whether the var is tmp, static, or global.
			fullPath = fullPath.replaceAll("/static", "");
			fullPath = fullPath.replaceAll("/global", "");
			// Parse the var definitions.
			if(fullPath.contains("var/") || 
					(fullPath.contains("=") && (!fullPath.contains("(") || fullPath.indexOf("(") > fullPath.indexOf("=")))) {
				String[] split = Pattern.compile("=").split(fullPath, 2);
				String varname = split[0].substring(split[0].lastIndexOf("/") + 1, split[0].length()).trim();
				if(split.length > 1) {
					String val = split[1].trim();
					String origVal = "";
					while(!origVal.equals(val)) {
						origVal = val;
						// Trust me, this is the fastest way to parse the macros.
						Matcher m = Pattern.compile("(?<![\\d\\w\"])\\w+(?![\\d\\w\"])").matcher(val);
						StringBuffer outVal = new StringBuffer();
						while(m.find()) {
							if(macros.containsKey(m.group(0)))
								m.appendReplacement(outVal, macros.get(m.group(0)));
							else
								m.appendReplacement(outVal, m.group(0));
						}
						m.appendTail(outVal);
						val = outVal.toString();
					}
					/*// Parse additions.
					Matcher m = Pattern.compile("([\\d\\.]+)[ \\t]*\\+[ \\t]*([\\d\\.]+)").matcher(val);
					StringBuffer outVal = new StringBuffer();
					while(m.find()) {
						m.appendReplacement(outVal, (Float.parseFloat(m.group(1)) + Float.parseFloat(m.group(2)))+"");
					}
					m.appendTail(outVal);
					val = outVal.toString();
					// Parse subtractions.
					m = Pattern.compile("([\\d\\.]+)[ \\t]*\\-[ \\t]*([\\d\\.]+)").matcher(val);
					outVal = new StringBuffer();
					while(m.find()) {
						m.appendReplacement(outVal, (Float.parseFloat(m.group(1)) - Float.parseFloat(m.group(2)))+"");
					}
					m.appendTail(outVal);
					val = outVal.toString();*/
					
					item.setVar(varname, val);
				} else {
					item.setVar(varname);
				}
			}
		}
		if(dlg != null)
			dlg.setVisible(false);
		
		// Reset variables
		isCommenting = false;
		inMultilineString = false;
		multilineStringDepth = 0;
		parenthesisDepth = 0;
		stringDepth = 0;
		stringExpDepth = 0;
		parenthesesDepth = 0; 
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
				if(c == '"' && nC == '}' && (pC != '\\' || ppC == '\\') && stringDepth == multilineStringDepth && inMultilineString)
					inMultilineString = false;
				if(c == '"' && (pC != '\\' || ppC == '\\') && stringDepth != stringExpDepth && (!inMultilineString || multilineStringDepth != stringDepth)) {
					stringDepth--;
				} else if(c == '"' && stringDepth == stringExpDepth && (!inMultilineString || multilineStringDepth != stringDepth)) {
					stringDepth++;
					if(pC == '{') {
						inMultilineString = true;
						multilineStringDepth = stringDepth;
					}
				}
				if(c == '[' && (pC != '\\' || ppC == '\\') && stringDepth != stringExpDepth)
					stringExpDepth++;
				if(c == ']' && stringDepth > 0 && stringDepth == stringExpDepth)
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
	
	public static String cleanPath(String s)
	{
		// Makes sure that paths start with a slash, and don't end with a slash.
		if(!s.startsWith("/"))
			s = "/" + s;
		if(s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		return s;
	}
}
