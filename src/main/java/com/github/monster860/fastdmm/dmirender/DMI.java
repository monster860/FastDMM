package com.github.monster860.fastdmm.dmirender;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;
import com.google.common.io.Files;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;

public class DMI {
    public int width = 32;
    public int height = 32;
    public int rows = 1;
    public int cols = 1;

    public Map<String, IconState> iconStates = new HashMap<>();
    public IconState defaultState;

    public int glID = -1;
    BufferedImage image;

    public DMI(InputStream inputStream) throws IOException {

        PngReader pngr = new PngReader(inputStream);
        ImageInfo info = pngr.getImgInfo();

        // There's a good reason I'm converting this into a bufferedimage and then loading into opengl:
        // I need to display this outside of opengl, and I need bufferedimage to do that.

        image = new BufferedImage(info.cols, info.rows, BufferedImage.TYPE_INT_ARGB);
        //ImageLineInt pngLine;
        DataBufferInt buffer = (DataBufferInt) image.getRaster().getDataBuffer();
        PngChunkPLTE pal = pngr.getMetadata().getPLTE();
        PngChunkTRNS trns = pngr.getMetadata().getTRNS();

        int[] data = buffer.getData();
        int dataindex = 0;

        // For converting to 8 bit-depth
        int mult = 1;
        int div = 1;
        switch (info.bitDepth) {
            case 1:
                mult = 255;
            case 2:
                mult = 85;
            case 4:
                mult = 17;
            case 16:
                div = 257;
        }
        if (info.indexed) {
            int tlen = trns != null ? trns.getPalletteAlpha().length : 0;
            while (pngr.hasMoreRows()) {
                ImageLineInt line = (ImageLineInt) pngr.readRow();
                int[] linearr = line.getScanline();
                for (int index : linearr) {
                    if (index < tlen) {
                        data[dataindex] = (int) (((long) trns.getPalletteAlpha()[index] << 24L) + (long) pal.getEntry(index));
                    } else {
                        data[dataindex] = (int) ((long) pal.getEntry(index) + 0xff000000L);
                    }
                    dataindex++;
                }
            }
        } else if (info.greyscale) {
            while (pngr.hasMoreRows()) {
                ImageLineInt line = (ImageLineInt) pngr.readRow();
                int[] linearr = line.getScanline();
                for (int i = 0; i < linearr.length; i += info.channels) {
                    int value = linearr[i] * mult / div; // Get the value
                    int alpha = 255;
                    if (info.channels == 2) {
                        alpha = linearr[i + 1] * mult / div;
                    }
                    data[dataindex] = value + (value << 8) + (value << 16) + (alpha << 24);
                    dataindex++;
                }
            }
        } else {
            while (pngr.hasMoreRows()) {
                ImageLineInt line = (ImageLineInt) pngr.readRow();
                int[] linearr = line.getScanline();
                for (int i = 0; i < linearr.length; i += info.channels) {
                    int r = linearr[i] * mult / div; // Get the value
                    int g = linearr[i + 1] * mult / div;
                    int b = linearr[i + 2] * mult / div;
                    int alpha = 255;
                    if (info.channels == 4) {
                        alpha = linearr[i + 3] * mult / div;
                    }
                    data[dataindex] = b + (g << 8) + (r << 16) + (alpha << 24);
                    dataindex++;
                }
            }
        }
        pngr.close();

        width = info.cols;
        height = info.rows;
        String textMeta = pngr.getMetadata().getTxtForKey("Description");
        String[] lines = textMeta.split("(\\r\\n|\\r|\\n)");
        IconState currState = null;
        List<IconState> statesList = new ArrayList<>();
        for (String line : lines) {
            String[] kv = line.split("=");
            if (kv.length < 2)
                continue;
            String key = kv[0].trim();
            String val = kv[1].trim();
            switch (key) {
                case "width":
                    int newWidth = Integer.parseInt(val);
                    cols = (width / newWidth);
                    width = newWidth;
                    break;
                case "height":
                    int newHeight = Integer.parseInt(val);
                    rows = (height / newHeight);
                    height = newHeight;
                    break;
                case "state":
                    Matcher m = Pattern.compile("\"(.*)\"").matcher(val);
                    if (m.find()) {
                        currState = new IconState(m.group(1));
                        if (defaultState == null || currState.name.equals(""))
                            defaultState = currState;
                        statesList.add(currState);
                    }
                    break;
                case "dirs":
                    currState.dirCount = Integer.parseInt(val);
                    break;
                case "frames":
                    currState.frameCount = Integer.parseInt(val);
                    break;
                case "delay":
                    String[] delayStrings = val.split(",");
                    if (delayStrings.length == currState.dirCount * currState.frameCount) {
                        currState.delays = new float[delayStrings.length];
                        for (int i = 0; i < delayStrings.length; i++) {
                            currState.delays[i] = Float.parseFloat(delayStrings[i]);
                        }
                    }
                    break;
            }
        }
        
        // Old-ish format support.
        if(statesList.size() > 1 && width == info.cols && height == info.rows) {
        	int frameCount = 0;
        	for(IconState i : statesList) {
        		frameCount += i.dirCount * i.frameCount;
        	}
        	width = info.cols / frameCount;
        	cols = frameCount;
        }
        
        // No icon data? Make one icon state.
        if (statesList.size() <= 0) {
            currState = new IconState("");
            defaultState = currState;
            statesList.add(currState);

        }
        int substateIndex = 0;
        for (IconState is : statesList) {
            IconSubstate[] substatesForState = new IconSubstate[is.dirCount * is.frameCount];
            for (int i = 0; i < is.dirCount * is.frameCount; i++) {
                substatesForState[i] = new IconSubstate(this, substateIndex);
                substateIndex++;
            }
            is.substates = substatesForState;
            iconStates.put(is.name, is);
        }

    }

    public DMI(File file) throws IOException {
        this(Files.asByteSource(file).openStream());
    }

    public IconState getIconState(String s) {
        if (iconStates.containsKey(s))
            return iconStates.get(s);
        else
            return defaultState;
    }

    public void createGL() {
        if (image == null || glID != -1)
            return;
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:
        glID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, glID);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }
}
