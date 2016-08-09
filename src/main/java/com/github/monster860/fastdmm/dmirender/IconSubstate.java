package com.github.monster860.fastdmm.dmirender;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

public class IconSubstate implements Icon {
	public DMI dmi;
	public int index;
	public int x;
	public int y;
	public float x1;
	public float x2;
	public float y1;
	public float y2;
	public int i_x1;
	public int i_x2;
	public int i_y1;
	public int i_y2;
	
	public static final float UV_MARGIN = .000001f;
	
	public IconSubstate(DMI dmi, int index) {
		this.dmi = dmi;
		this.index = index;
		x = (index % dmi.cols);
		y = (int) Math.floor((float)index / (float)dmi.cols);
		x1 = ((float)x / (float)dmi.cols) + UV_MARGIN;
		y1 = ((float)y / (float)dmi.rows) + UV_MARGIN;
		x2 = ((float)(x + 1) / (float)dmi.cols) - UV_MARGIN;
		y2 = ((float)(y + 1) / (float)dmi.rows) - UV_MARGIN;
		i_x1 = x * dmi.width;
		i_y1 = y * dmi.height;
		i_x2 = (x+1) * dmi.width - 1;
		i_y2 = (y+1) * dmi.height - 1;
	}

	@Override
	public int getIconHeight() {
		// TODO Auto-generated method stub
		return dmi.height;
	}

	@Override
	public int getIconWidth() {
		// TODO Auto-generated method stub
		return dmi.width;
	}
	
	// Return true if substate is fully opaque.
	public boolean isOpaque() {
		if(dmi.image == null)
			return true;
		for(int px = i_x1; px <= i_x2; px++)
			for(int py = i_y1; py <= i_y2; py++) {
				if(((dmi.image.getRGB(px,py) >> 24) & 0xFF) < 200) {
					return false;
				}
			}
		return true;
	}

	@Override
	public void paintIcon(Component arg0, Graphics arg1, int px, int py) {
		arg1.drawImage(dmi.image, px, py, px+dmi.width-1, py+dmi.height-1, i_x1, i_y1, i_x2, i_y2, arg0);
	}
	
	Scaled cachedScaled;
	
	public Icon getScaled() {
		if(cachedScaled == null)
			cachedScaled = new Scaled(this);
		return cachedScaled;
	}
	
	public static class Scaled implements Icon {
		IconSubstate parent;
		float scalingFactor = .5f;
		public Scaled(IconSubstate parent) {
			this.parent = parent;
			scalingFactor = 16f / parent.dmi.height;
		}
		
		@Override
		public int getIconHeight() {
			return (int)(parent.dmi.height*scalingFactor);
		}

		@Override
		public int getIconWidth() {
			return (int)(parent.dmi.width*scalingFactor);
		}

		@Override
		public void paintIcon(Component c, Graphics g, int px, int py) {
			if(g instanceof Graphics2D) {
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
			g.drawImage(parent.dmi.image, px, py, px+getIconWidth()-1, py+getIconHeight()-1, parent.i_x1, parent.i_y1, parent.i_x2, parent.i_y2, c);
		}
		
	}
}
