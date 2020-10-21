package components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import components.DSComponent;
import remote.IWhiteBoardClient;

@SuppressWarnings("serial")
public class LineComponent extends Line2D.Double implements DSComponent {

	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private boolean inModification = false;
	private String username;
	private int modificationType = DSComponent.ERROR;
	private Point translationStart;
	private Color color;
	private int thickness;
	private JButton deleteButton;
	private String id;

	public LineComponent(int x1, int y1, int x2, int y2, Color color, int thickness) {
		super(x1, y1, x2, y2);
		this.id = UUID.randomUUID().toString();

		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
		this.thickness = thickness;

		createButton();
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
		this.deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete(whiteBoard);
			}
		});
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
		g.draw(this);

		if (this.inModification) {
			if (this.username.equals(username)) {

				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.BLACK);

				g2.setStroke(new BasicStroke(1));
				g2.drawOval(this.x1 - 5, this.y1 - 5, 10, 10);
				g2.drawOval(this.x2 - 5, this.y2 - 5, 10, 10);

				g2.dispose();
				this.deleteButton.setBounds(x2 - 13, y1 - 23, 20, 17);
			} else {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.BLUE);

				Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6 },
						0);
				g2.setStroke(dashed);
				g2.draw(this.getBounds());

				g2.setStroke(new BasicStroke(2));
				g2.setFont(new Font("", Font.BOLD, 12));
				g2.drawString(this.username, x1, y1 - 10);

				g2.dispose();
			}
		}
	}

	@Override
	public void setEnd(int x, int y) {
		this.x2 = x;
		this.y2 = y;
		this.setLine(x1, y1, x2, y2);
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
			try {
				whiteBoard.remove(this.deleteButton);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return (this.ptLineDist(x, y) < 10);
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
		} else if (this.contains(x, y)) {
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
		if (this.modificationType == DSComponent.TOP_LEFT) {
			this.x1 = x;
			this.y1 = y;
			this.setLine(x1, y1, x2, y2);
		} else if (this.modificationType == DSComponent.BOTTOM_RIGHT) {
			this.x2 = x;
			this.y2 = y;
			this.setLine(x1, y1, x2, y2);
		} else if (this.modificationType == DSComponent.TOP_RIGHT) {
			this.x2 = x;
			this.y1 = y;
			this.setLine(x1, y1, x2, y2);
		} else if (this.modificationType == DSComponent.BOTTOM_LEFT) {
			this.x1 = x;
			this.y2 = y;
			this.setLine(x1, y1, x2, y2);
		} else if (this.modificationType == DSComponent.CENTER) {
			int xMove = x - this.translationStart.x;
			int yMove = y - this.translationStart.y;
			this.x1 += xMove;
			this.y1 += yMove;
			this.x2 += xMove;
			this.y2 += yMove;
			this.translationStart.setLocation(x, y);
			this.setLine(x1, y1, x2, y2);
		}
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
				} else if (this.contains(x, y)) {
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
	public boolean isInEdit() {
		return inModification;
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
	public boolean equals(Object obj) {
		if (obj instanceof LineComponent) {
			return this.id.equals(((LineComponent) obj).id);
		} else {
			return false;
		}
	}

}
