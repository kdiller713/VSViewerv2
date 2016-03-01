package Panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.vecmath.Point3d;

import Protein.Protein;
import Util.Database;
import Util.KFileChooser;
import Util.VirtualViewer;
import Views.ThreeDimView;

@SuppressWarnings("serial")
public class ThreeDimPanel extends JPanel implements Observer {
	private VirtualViewer vv;
	private Database data;
	private boolean deletable;

	private JCheckBox[] display;
	private JButton[] remove;
	private boolean[] displayed;
	private JPanel[] mol;

	public ThreeDimPanel(Database d, ActionListener a, boolean readAll,
			boolean delete) {
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(900, 700));
		data = d;
		data.addObserver(this);
		deletable = delete;

		Point3d middle = new Point3d(data.getMiddle());

		vv = new VirtualViewer(middle);
		vv.addZoom();
		vv.addRotate();
		vv.addMove();

		this.add(BorderLayout.CENTER, vv);

		display = new JCheckBox[data.getNumMols()];
		displayed = new boolean[data.getNumMols()];
		remove = new JButton[data.getNumMols()];
		mol = new JPanel[data.getNumMols()];

		Box checkboxes = Box.createVerticalBox();

		for (int i = 0; i < display.length; i++) {
			mol[i] = new JPanel();

			display[i] = new JCheckBox(data.getMolecule(i).getName());
			display[i].addActionListener(a);

			remove[i] = new JButton("X");
			remove[i].addActionListener(a);

			displayed[i] = readAll;
			mol[i].add(display[i]);
			mol[i].add(remove[i]);
			remove[i].setVisible(deletable);
			mol[i].setVisible(readAll);

			checkboxes.add(mol[i]);
		}

		JScrollPane dataPane = new JScrollPane(checkboxes);
		dataPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		dataPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		dataPane.setPreferredSize(new Dimension(200, 700));

		this.add(BorderLayout.EAST, dataPane);
	}

	public void addMolecule(int mol) {
		if (mol < 0)
			return;

		if (!display[mol].isSelected())
			vv.addBranchGroup(data.getMolecule(mol));
		displayed[mol] = true;
		this.mol[mol].setVisible(true);
		display[mol].setSelected(true);
	}

	public boolean display() {
		return data.display(3);
	}

	public void remove(int i) {
		displayed[i] = false;
		mol[i].setVisible(false);

		if (display[i].isSelected())
			vv.removeBranchGroup(data.getMolecule(i));
		display[i].setSelected(false);
	}

	public void findChangedBox(Object source) {
		int i = 0;

		for (i = 0; i < display.length; i++) {
			if (display[i] == source)
				break;
		}

		if (i == display.length)
			return;

		if (display[i].isSelected()) {
			vv.addBranchGroup(data.getMolecule(i));
		} else {
			vv.removeBranchGroup(data.getMolecule(i));
		}
	}

	public void findButton(Object source) {
		int i = 0;

		for (i = 0; i < remove.length; i++) {
			if (remove[i] == source)
				break;
		}

		if (i == remove.length)
			return;

		remove(i);
	}

	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		KFileChooser k = KFileChooser.create();

		if (source instanceof JCheckBox)
			findChangedBox(source);
		else if (source instanceof JButton)
			findButton(source);
		else
			switch (arg0.getActionCommand().charAt(0)) {
			case 'S':
				if (k.save(this, 2) == JFileChooser.APPROVE_OPTION) {
					BufferedImage i = (BufferedImage) vv.getSnapshot(
							vv.getWidth(), vv.getHeight());

					try {
						ImageIO.write(i, "png", k.getFile());
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				break;
			case 'O':
				if (k.open(this, 1) != JFileChooser.APPROVE_OPTION) {
					break;
				}

				try {
					Protein p = Protein.read(k.getFile().toString());
					vv.addBranchGroup(p);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		this.repaint();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		((ThreeDimView) display[0].getActionListeners()[0]).show(true);
	}
}