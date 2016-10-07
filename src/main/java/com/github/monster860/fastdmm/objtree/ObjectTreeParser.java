package com.github.monster860.fastdmm.objtree;

import com.github.monster860.fastdmm.CachedPattern;
import com.github.monster860.fastdmm.Util;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
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
	int[] arrayDepth = new int[50];
	
	public ObjectTree tree;
	
	public Map<String, String> macros = new HashMap<>();
	public JFrame modalParent;

    private static final CachedPattern QUOTES_PATTERN = new CachedPattern("^\"(.*)\"$");
	private static final CachedPattern DEFINE_PATTERN = new CachedPattern("#define +([\\d\\w]+) +(.+)");
	private static final CachedPattern UNDEF_PATTERN  = new CachedPattern("#undef[ \\t]*([\\d\\w]+)");
	private static final CachedPattern MACRO_PATTERN  = new CachedPattern("(?<![\\d\\w\"])\\w+(?![\\d\\w\"])");

    public ObjectTreeParser() {
		tree = new ObjectTree();
	}

	public ObjectTreeParser(ObjectTree tree) {
		this.tree = tree;
	}

	public void parseDME(File file) throws IOException {
		// Parse stddef.dm for macros and such.
		doSubParse(new BufferedReader(new InputStreamReader(Util.getFile("stddef.dm"))), Paths.get("stddef.dm"));

		doParse(new BufferedReader(new FileReader(file)), file.toPath(), true);
	}

	public void doParse(BufferedReader br, Path currentFile, boolean isMainFile) throws IOException
	{
		String line = null;
		ArrayList<String> lines = new ArrayList<>();
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

		ArrayList<String> pathTree = new ArrayList<>();

		int currentInclude = 0;

		JProgressBar dpb = null;
		JDialog dlg = null;
		JLabel lbl = null;
		if(isMainFile) {
			final JDialog tdlg = new JDialog(modalParent, "Object Tree Generation", modalParent != null);
			dlg = tdlg;
			dpb = new JProgressBar(0, includeCount);
			dlg.add(BorderLayout.CENTER, dpb);
			lbl = new JLabel("");
			dlg.add(BorderLayout.NORTH, lbl);
			dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dlg.setSize(300, 75);
			Thread t = new Thread(() -> {
                tdlg.setVisible(true);
            });
			t.start();
		}

        for (String line1 : lines) {
            line = line1;
            // Process #include, #define, and #undef
            if (line.trim().startsWith("#")) {
                line = line.trim();
                if (line.startsWith("#include")) {
                    String path = line.split("\"")[1];
                    if (isMainFile) {
                        lbl.setText(path);
                    }
                    if (path.endsWith(".dm") || path.endsWith(".dme")) {
                        File includeFile = new File(currentFile.getParent().toFile(), Util.separatorsToSystem(path));
                        if (!includeFile.exists()) {
                            System.err.println(currentFile.getFileName() + " references a nonexistent file: " + includeFile.getAbsolutePath());
                            continue;
                        }
	                    doSubParse(new BufferedReader(new FileReader(includeFile)), includeFile.toPath());
                    }
                    if (isMainFile) {
                        currentInclude++;
                        dpb.setValue(currentInclude);
                    }
                }
                else if (line.startsWith("#define")) {
                    Matcher m = DEFINE_PATTERN.getMatcher(line);
                    if (m.find()) {
						String group = m.group(1);
						if (group.equals("FILE_DIR")) {
							Matcher quotes = QUOTES_PATTERN.getMatcher(m.group(2));
							if (quotes.find()) {
								// 2 ways this can't happen:
								// Somebody intentionally placed broken FILE_DIR defines.
								// It's the . FILE_DIR, which has no quotes, and we don't need.
								tree.fileDirs.add(Paths.get(Util.separatorsToSystem(quotes.group(1))));
							}

						} else {
							macros.put(m.group(1), m.group(2).replace("$", "\\$"));
						}
                    }
                }
                else if (line.startsWith("#undef")) {
                    Matcher m = UNDEF_PATTERN.getMatcher(line);
                    if (m.find() && macros.containsKey(m.group(1))) {
                        macros.remove(m.group(1));
                    }
                }

                continue;
            }
            // How far is this line indented?
            int level = 0;
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == ' ')
                    level++;
                else
                    break;
            }
            // Rebuild the path tree.
            for (int j = pathTree.size(); j <= level; j++)
                pathTree.add("");
            pathTree.set(level, cleanPath(line.trim()));
            if (pathTree.size() > level + 1)
                for (int j = pathTree.size() - 1; j > level; j--)
                    pathTree.remove(j);
            String fullPath = "";
            for (String c : pathTree)
                fullPath += c;
            // Now, split it again, and rebuild it again, but only figure out how big the object itself is.
            String[] divided = fullPath.split("\\/");
            String affectedObjectPath = "";
            for (String item : divided) {
                if (item.isEmpty()) {
                    continue;
                }
                if (item.equalsIgnoreCase("static") || item.equalsIgnoreCase("global") || item.equalsIgnoreCase("tmp"))
                    continue;
                if (item.equals("proc") || item.equals("verb") || item.equals("var")) {
                    break;
                }
                if (item.contains("=") || item.contains("(")) {
                    break;
                }
                affectedObjectPath += "/" + item;
            }
            ObjectTree.Item item = tree.getOrCreate(affectedObjectPath);
            if (fullPath.contains("(") && fullPath.indexOf("(") < fullPath.lastIndexOf("/"))
                continue;
            fullPath = fullPath.replaceAll("/tmp", ""); // Let's avoid giving a shit about whether the var is tmp, static, or global.
            fullPath = fullPath.replaceAll("/static", "");
            fullPath = fullPath.replaceAll("/global", "");
            // Parse the var definitions.
            if (fullPath.contains("var/") ||
                    (fullPath.contains("=") && (!fullPath.contains("(") || fullPath.indexOf("(") > fullPath.indexOf("=")))) {
                String[] split = Pattern.compile("=").split(fullPath, 2);
                String varname = split[0].substring(split[0].lastIndexOf("/") + 1, split[0].length()).trim();
                if (split.length > 1) {
                    String val = split[1].trim();
                    String origVal = "";
                    while (!origVal.equals(val)) {
                        origVal = val;
                        // Trust me, this is the fastest way to parse the macros.
                        Matcher m = MACRO_PATTERN.getMatcher(val);
                        StringBuffer outVal = new StringBuffer();
                        while (m.find()) {
                            if (macros.containsKey(m.group(0)))
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
		arrayDepth = new int[50];
	}

	private void doSubParse(BufferedReader br, Path currentFile) throws IOException {
		ObjectTreeParser parser = new ObjectTreeParser(tree);
		parser.macros = macros;
		parser.doParse(br, currentFile, false);
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
