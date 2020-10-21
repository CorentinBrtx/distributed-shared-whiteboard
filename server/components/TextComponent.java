package components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import components.DSComponent;
import main.WhiteBoard;
import remote.IWhiteBoardClient;

@SuppressWarnings("serial")
public class TextComponent extends JPanel implements DSComponent {

	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private JTextArea textArea;
	private Rectangle rectContainer;
	private boolean inModification = false;
	private String username;
	private Color color;
	private int thickness;
	private JButton deleteButton;
	private boolean inWriting = true;
	private int modificationType = DSComponent.ERROR;
	private Point translationStart;
	private String id;

	public TextComponent(int x, int y, Color color, int thickness) {

		this.id = UUID.randomUUID().toString();

		this.setLayout(new BorderLayout());
		this.textArea = new JTextArea();
		this.textArea.setLineWrap(true);
		this.add(textArea);
		this.x1 = x;
		this.y1 = y;
		this.x2 = x + 120;
		this.y2 = y + 80;
		this.color = color;
		this.thickness = thickness;
		this.rectContainer = new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		this.setOpaque(false);

		createButton();
		this.revalidate();
	}

	public void createButton() {
		this.deleteButton = new JButton();
		URL imageURL = getClass().getResource("/images/close.png");
		if (imageURL != null) { // image found

			ImageIcon imageIcon = new ImageIcon(imageURL);
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back
			this.deleteButton.setIcon(imageIcon);
		} else { // no image found
			this.deleteButton.setText("X");
			System.err.println("Resource not found: close.png");
		}
		this.deleteButton.setContentAreaFilled(false);
		this.deleteButton.setBorderPainted(false);

		this.deleteButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				deleteButton.setFocusPainted(true);
				deleteButton.requestFocus(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				deleteButton.setFocusPainted(false);
			}

		});
	}

	public void updateButton(IWhiteBoardClient whiteBoard) {
		this.deleteButton.addActionListener(event -> delete(whiteBoard));
		try {
			whiteBoard.add(deleteButton);
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected void delete(IWhiteBoardClient whiteBoard) {
		try {
			whiteBoard.remove(this.deleteButton);
			whiteBoard.delete(this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void draw(Graphics2D g, String username) {
		g.setColor(this.color);
		g.setStroke(new BasicStroke(this.thickness));

		this.removeAll();

		this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		this.rectContainer.setFrame(Math.min(x1, x2) - 1, Math.min(y1, y2) - 1, Math.abs(x2 - x1) + 2,
				Math.abs(y2 - y1) + 2);
		this.textArea.setBounds(0, 0, Math.abs(x2 - x1), Math.abs(y2 - y1));

		g.draw(this.rectContainer);

		if (!this.username.equals(username)) {
			g.drawImage(this.getImage(), Math.min(x1, x2), Math.min(y1, y2), this);
			if (this.inWriting || this.inModification) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.BLUE);

				Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6 },
						0);
				g2.setStroke(dashed);
				g2.draw(this.getBounds());

				g2.setStroke(new BasicStroke(2));
				g2.setFont(new Font("", Font.BOLD, 12));
				g2.drawString(this.username, (int) x1, (int) y1 - 10);

				g2.dispose();
			}
		} else if (this.inWriting) {
			this.add(this.textArea);
			this.textArea.requestFocusInWindow();
		} else if (this.inModification) {

			g.drawImage(this.getImage(), Math.min(x1, x2), Math.min(y1, y2), this);

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setColor(Color.BLACK);

			// set the stroke of the copy, not the original
			Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
			g2.setStroke(dashed);
			g2.draw(this.getBounds());

			g2.setStroke(new BasicStroke(1));
			g2.drawOval(this.x1 - 5, this.y1 - 5, 10, 10);
			g2.drawOval(this.x1 - 5, this.y2 - 5, 10, 10);
			g2.drawOval(this.x2 - 5, this.y1 - 5, 10, 10);
			g2.drawOval(this.x2 - 5, this.y2 - 5, 10, 10);

			g2.dispose();
			this.deleteButton.setBounds(x2 - 13, y1 - 23, 20, 17);
		}
	}

	@Override
	public void setEnd(int x, int y) {
	}

	public void startEditing(IWhiteBoardClient whiteBoard, String username) {
		this.inModification = true;
		this.username = username;
		try {
			whiteBoard.add(this.deleteButton);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopEditing(IWhiteBoardClient whiteBoard, String username) {
		if (username.equals(this.username)) {
			this.inModification = false;
			this.username = "";
			try {
				whiteBoard.remove(this.deleteButton);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateEditType(int x, int y) {
		if (Point2D.distance(this.x1, this.y1, x, y) <= 5) {
			this.modificationType = DSComponent.TOP_LEFT;
		} else if (Point2D.distance(this.x2, this.y2, x, y) <= 5) {
			this.modificationType = DSComponent.BOTTOM_RIGHT;
		} else if (Point2D.distance(this.x2, this.y1, x, y) <= 5) {
			this.modificationType = DSComponent.TOP_RIGHT;
		} else if (Point2D.distance(this.x1, this.y2, x, y) <= 5) {
			this.modificationType = DSComponent.BOTTOM_LEFT;
		} else if (this.contains(x - this.x1, y - this.y1)) {
			this.modificationType = DSComponent.CENTER;
			this.translationStart = new Point(x, y);
		} else {
			this.modificationType = DSComponent.ERROR;
		}
	}

	@Override
	public void resetEditType() {
		this.modificationType = DSComponent.ERROR;
	}

	@Override
	public void editShape(int x, int y) {
		if (this.modificationType == DSComponent.TOP_LEFT && (x < x2) && (y < y2)) {
			this.x1 = x;
			this.y1 = y;
			this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		} else if (this.modificationType == DSComponent.BOTTOM_RIGHT && (x > x1) && (y > y1)) {
			this.x2 = x;
			this.y2 = y;
			this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		} else if (this.modificationType == DSComponent.TOP_RIGHT && (x > x1) && (y < y2)) {
			this.x2 = x;
			this.y1 = y;
			this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		} else if (this.modificationType == DSComponent.BOTTOM_LEFT && (x < x2) && (y > y1)) {
			this.x1 = x;
			this.y2 = y;
			this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		} else if (this.modificationType == DSComponent.CENTER) {
			int xMove = x - this.translationStart.x;
			int yMove = y - this.translationStart.y;
			this.x1 += xMove;
			this.y1 += yMove;
			this.x2 += xMove;
			this.y2 += yMove;
			this.translationStart.setLocation(x, y);
			this.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
		}
	}

	public void startWriting(String username, WhiteBoard whiteBoard) {
		this.inWriting = true;
		this.username = username;
	}

	public void stopWriting(String username) {
		inWriting = false;
		this.username = "";
	}

	public Image getImage() {
		BufferedImage img = new BufferedImage(this.textArea.getWidth(), textArea.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		textArea.setCaretColor(Color.WHITE);
		textArea.printAll(g2d);
		textArea.setCaretColor(Color.BLACK);
		g2d.dispose();

		return (Image) img;
	}

	@Override
	public void updateMouseCursor(IWhiteBoardClient whiteBoard, int x, int y) {
		try {
			if (this.modificationType == DSComponent.TOP_LEFT) {
				whiteBoard.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
			} else if (this.modificationType == DSComponent.BOTTOM_RIGHT) {
				whiteBoard.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
			} else if (this.modificationType == DSComponent.TOP_RIGHT) {
				whiteBoard.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
			} else if (this.modificationType == DSComponent.BOTTOM_LEFT) {
				whiteBoard.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
			} else if (this.modificationType == DSComponent.CENTER) {
				whiteBoard.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			} else {
				if (Point2D.distance(this.x1, this.y1, x, y) <= 5) {
					whiteBoard.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
				} else if (Point2D.distance(this.x2, this.y2, x, y) <= 5) {
					whiteBoard.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
				} else if (Point2D.distance(this.x2, this.y1, x, y) <= 5) {
					whiteBoard.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
				} else if (Point2D.distance(this.x1, this.y2, x, y) <= 5) {
					whiteBoard.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
				} else if (this.contains(x - this.x1, y - this.y1)) {
					whiteBoard.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				} else {
					whiteBoard.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public int getThickness() {
		return thickness;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isInEdit() {
		return inModification;
	}

	public boolean isInWriting() {
		return inWriting;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextComponent) {
			return this.id.equals(((TextComponent) obj).id);
		} else {
			return false;
		}
	}

	@Override
	public Rectangle2D getBounds2D() {
		return this.rectContainer;
	}

	@Override
	public boolean contains(double x, double y) {
		return this.rectContainer.contains(x, y);
	}

	@Override
	public boolean contains(Point2D p) {
		return this.rectContainer.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		return false;
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		return false;
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return this.rectContainer.contains(x, y, w, h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		return this.rectContainer.contains(r);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return null;
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return null;
	}

}
