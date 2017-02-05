package com.github.monster860.fastdmm.editing.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import com.github.monster860.fastdmm.FastDMM;
import com.github.monster860.fastdmm.dmmmap.DMM;

public class EditorTabComponent extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private FastDMM editor;
	private DMM map;
	
	public EditorTabComponent(FastDMM editor, DMM map) {
		super(new GridBagLayout());
		this.editor = editor;
		this.map = map;
		setOpaque(false);
		JLabel title = new JLabel(map.relPath);
		JButton close = new CloseButton();
		close.setPreferredSize(new Dimension(20, 20));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		
		add(title, gbc);
		
		gbc.gridx++;
		gbc.weightx = 0;
		add(close, gbc);
		
		close.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		editor.closeTab(map);
	}
	
	public static class CloseButton extends JButton {
		private static final long serialVersionUID = 1L;
		public CloseButton() {
			setToolTipText("Close");
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setBorder(BorderFactory.createLineBorder(Color.gray));
			setBorderPainted(false);
			addMouseListener(new MouseAdapter() {
			    @Override
			    public void mouseEntered(MouseEvent e) {
			        CloseButton button = CloseButton.this;
			        button.setBorderPainted(true);
			    }

			    @Override
			    public void mouseExited(MouseEvent e) {
			        CloseButton button = CloseButton.this;
			        button.setBorderPainted(false);
			    }
			});
			setRolloverEnabled(true);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(11, 11);
		}
		
		@Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        Graphics2D g2 = (Graphics2D) g.create();
	        if(getModel().isRollover()) {
	        	g2.setColor(new Color(255, 192, 192));
	        	g2.fillRect(0, 0, getWidth(), getHeight());
	        }
	        
	        if (getModel().isPressed()) {
	            g2.translate(1, 1);
	        }
	        g2.setStroke(new BasicStroke(1));
	        g2.setColor(new Color(126, 118, 91));
	        
	        int delta = 2;
	        g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
	        g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
	        g2.dispose();
	    }
	}
}
