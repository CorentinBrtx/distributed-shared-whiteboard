package main;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class DSToolBar extends JToolBar {

	private JButton selectButton;
	private JButton freeLineButton;
	private JButton lineButton;
	private JButton ovalButton;
	private JButton rectButton;
	private JButton textButton;
	private JButton colorButton;
	private JSpinner spinner;

	public DSToolBar(WhiteBoard whiteBoard) {

		this.selectButton = this.makeButton("select.png", "Selection tool", "Selection");
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterSelectionMode();
			}
		});
		this.add(selectButton);

		this.freeLineButton = this.makeButton("free_line.png", "Free line tool", "Free line");
		freeLineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterDrawingMode();
				whiteBoard.setDrawingMode(WhiteBoard.FREE_LINE);
			}
		});
		this.add(freeLineButton);

		this.lineButton = this.makeButton("line.jpg", "Line tool", "Line");
		lineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterDrawingMode();
				whiteBoard.setDrawingMode(WhiteBoard.LINE);
			}
		});
		this.add(lineButton);

		this.ovalButton = this.makeButton("oval.png", "Oval tool", "Oval");
		ovalButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterDrawingMode();
				whiteBoard.setDrawingMode(WhiteBoard.OVAL);
			}
		});
		this.add(ovalButton);

		this.rectButton = this.makeButton("rectangle.png", "Rectangle tool", "Rectangle");
		rectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterDrawingMode();
				whiteBoard.setDrawingMode(WhiteBoard.RECTANGLE);
			}
		});
		this.add(rectButton);

		this.textButton = this.makeButton("text.png", "Text area tool", "Text area");
		textButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.enterDrawingMode();
				whiteBoard.setDrawingMode(WhiteBoard.TEXT_AREA);
			}
		});
		this.add(textButton);

		this.colorButton = new JButton();

		BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setPaint(whiteBoard.getColor());
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		ImageIcon imageIcon = new ImageIcon(image);
		colorButton.setIcon(imageIcon);

		colorButton.setFocusable(false);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				whiteBoard.setColor(JColorChooser.showDialog(whiteBoard, "Choose a color", whiteBoard.getColor()));
				BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = image.createGraphics();

				graphics.setPaint(whiteBoard.getColor());
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

				ImageIcon imageIcon = new ImageIcon(image);
				colorButton.setIcon(imageIcon);
			}
		});
		this.add(colorButton);

		SpinnerModel value = new SpinnerNumberModel(1, 1, 500, 1);
		this.spinner = new JSpinner(value);
		this.spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				whiteBoard.setThickness((Integer) spinner.getValue());
			}
		});

		this.add(spinner);

	}

	protected JButton makeButton(String imageName, String toolTipText, String altText) {
		// Look for the image.
		String imgLocation = "/images/" + imageName;
		URL imageURL = getClass().getResource(imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setToolTipText(toolTipText);

		if (imageURL != null) { // image found
			ImageIcon imageIcon = new ImageIcon(imageURL, altText);
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back
			button.setIcon(imageIcon);
		} else { // no image found
			button.setText(altText);
			System.err.println("Resource not found: " + imgLocation);
		}
		return button;
	}

	public void setThicknessValue(int thickness) {
		this.spinner.setValue(thickness);
	}

}
