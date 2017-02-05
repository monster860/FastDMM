package com.github.monster860.fastdmm.editing.ui;

import java.awt.*;
import javax.swing.JPanel;

public class EmptyTabPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JPanel parentPanel;
	public EmptyTabPanel(JPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
		setLayout(new BorderLayout());
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(parentPanel.getWidth(), 0);
	}
}
