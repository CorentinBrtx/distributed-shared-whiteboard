package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import components.DSComponent;
import components.FreeLineComponent;
import components.LineComponent;
import components.OvalComponent;
import components.RectComponent;
import components.TextComponent;
import remote.IWhiteBoardClient;
import remote.IWhiteBoardServer;

@SuppressWarnings("serial")
public class WhiteBoard extends JPanel implements IWhiteBoardClient {

	private ArrayList<DSComponent> components = new ArrayList<DSComponent>();
	private String username;
	private DSComponent newComponent;
	private DSComponent componentInEdit;
	private DSComponent componentInWriting;
	private IWhiteBoardServer server;
	private IWindow window;
	private DrawingListener drawingMouseListener;
	private SelectionListener selectionMouseListener;
	private int mode = 1;
	public final static int SELECT = 0;
	public final static int FREE_LINE = 1;
	public final static int LINE = 2;
	public final static int OVAL = 3;
	public final static int RECTANGLE = 4;
	public final static int TEXT_AREA = 5;
	private Color color = Color.BLACK;
	private int thickness = 1;
	private boolean availableForRefresh = true;
	private boolean offline = false;
	private int creating = 0;

	public WhiteBoard(IWindow window, String username, IWhiteBoardServer server) {

		this.window = window;
		this.server = server;
		this.username = username;

		this.drawingMouseListener = new DrawingListener(this);
		this.selectionMouseListener = new SelectionListener(this);

		this.addMouseListener(this.selectionMouseListener);
		this.addMouseMotionListener(this.selectionMouseListener);

		this.setBackground(Color.WHITE);

	}

	@Override
	protected synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));
		for (DSComponent component : components) {
			if (!(component == null)) {
				component.draw(g2d, this.username);
			}

		}
	}

	public synchronized void createNewComponent(int x, int y) {

		stopEditing();
		stopWriting();

		creating += 1;

		if (mode == WhiteBoard.FREE_LINE) {
			newComponent = new FreeLineComponent(x, y, color, thickness);
		} else if (mode == WhiteBoard.LINE) {
			newComponent = new LineComponent(x, y, x, y, color, thickness);
		} else if (mode == WhiteBoard.OVAL) {
			newComponent = new OvalComponent(x, y, x, y, color, thickness);
		} else if (mode == WhiteBoard.RECTANGLE) {
			newComponent = new RectComponent(x, y, x, y, color, thickness);
		} else if (mode == WhiteBoard.TEXT_AREA) {
			newComponent = new TextComponent(x, y, color, thickness);
			((TextComponent) newComponent).startWriting(username, this);
			componentInWriting = newComponent;
			this.add((JPanel) newComponent);
			try {
				TimeUnit.MILLISECONDS.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		newComponent.setUsername(username);
		components.add(newComponent);
		if (!offline) {
			new Thread(() -> {
				try {
					while (!availableForRefresh) {
						try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					availableForRefresh = false;
					server.addNewComponent(username, newComponent);
					if (newComponent instanceof TextComponent) {
						newComponent = null;
					}
					availableForRefresh = true;
					creating -= 1;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		}
		this.repaint();

	}

	public synchronized void editNewComponent(int x, int y) {
		if (newComponent != null && !(newComponent instanceof TextComponent)) {
			newComponent.setEnd(x, y);
			if (!offline) {
				new Thread(() -> {
					try {
						if (availableForRefresh) {
							availableForRefresh = false;
							server.updateComponent(username, newComponent);
							availableForRefresh = true;
						}

					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
			}
			this.repaint();
		}
	}

	public synchronized void completeNewComponent(int x, int y) {
		if (newComponent != null && !(newComponent instanceof TextComponent)) {
			newComponent.setEnd(x, y);
			if (!(newComponent instanceof TextComponent)) {
				newComponent.setUsername("");
			}
			if (!offline) {
				try {
					while (!availableForRefresh || (creating != 0)) {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					server.updateComponent(username, newComponent);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			newComponent = null;
		}
		this.repaint();

	}

	public synchronized void stopEditing() {
		if (componentInEdit != null) {
			componentInEdit.stopEditing(this, username);
			if (!offline) {
				try {
					while (!availableForRefresh || (creating != 0)) {
						try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					availableForRefresh = false;

					server.updateComponent(username, componentInEdit);
					availableForRefresh = true;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			componentInEdit = null;
		}
		this.repaint();
	}

	public synchronized void stopWriting() {
		if (componentInWriting != null) {
			((TextComponent) componentInWriting).stopWriting(username);
			if (!offline) {
				try {
					while (!availableForRefresh || (creating != 0)) {
						try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					availableForRefresh = false;

					server.updateComponent(username, componentInWriting);
					componentInWriting = null;
					availableForRefresh = true;
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.repaint();
	}

	public void setDrawingMode(int mode) {
		this.mode = mode;
		this.stopEditing();
		this.stopWriting();
	}

	public void enterDrawingMode() {

		stopEditing();
		stopWriting();

		this.removeMouseListener(selectionMouseListener);
		this.removeMouseMotionListener(selectionMouseListener);
		this.removeMouseListener(drawingMouseListener);
		this.removeMouseMotionListener(drawingMouseListener);
		this.addMouseListener(drawingMouseListener);
		this.addMouseMotionListener(drawingMouseListener);

		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	public void enterSelectionMode() {

		stopEditing();
		stopWriting();

		this.removeMouseListener(selectionMouseListener);
		this.removeMouseMotionListener(selectionMouseListener);
		this.removeMouseListener(drawingMouseListener);
		this.removeMouseMotionListener(drawingMouseListener);
		this.addMouseListener(selectionMouseListener);
		this.addMouseMotionListener(selectionMouseListener);

		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

	}

	public synchronized void selectShape(int x, int y) {

		stopWriting();
		stopEditing();

		for (DSComponent component : components) {
			if (component.contains(x, y)) {
				this.componentInEdit = component;
			}
		}

		if (componentInEdit != null) {
			if (!offline) {
				try {
//					while (!availableForRefresh) {
//					}
					if (server.availableForEdit(username, componentInEdit)) {
						componentInEdit.updateButton(this);
						componentInEdit.startEditing(this, username);

						window.getToolBar().setThicknessValue(this.componentInEdit.getThickness());

						server.updateComponent(username, componentInEdit);
					} else {
						this.componentInEdit = null;
					}

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				componentInEdit.updateButton(this);
				componentInEdit.startEditing(this, username);

				window.getToolBar().setThicknessValue(this.componentInEdit.getThickness());
			}
		}
		this.updateSelectionMouseCursor(x, y);
		this.repaint();
	}

	public synchronized void editShape(int x, int y) {
		if (componentInEdit != null) {
			componentInEdit.editShape(x, y);
			if (!offline) {
				new Thread(() -> {
					try {
						if (availableForRefresh) {
							availableForRefresh = false;
							server.updateComponent(username, componentInEdit);
							availableForRefresh = true;
						}

					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
			}

		}
		if (componentInWriting != null) {
			if (!offline) {
				new Thread(() -> {
					try {
						if (availableForRefresh) {
							availableForRefresh = false;
							server.updateComponent(username, componentInWriting);
							availableForRefresh = true;
						}

					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
			}
		}
		this.repaint();
	}

	public synchronized void selectText(int x, int y) {

		stopEditing();
		stopWriting();

		TextComponent selectedTextArea = null;
		for (DSComponent component : components) {
			if (component.contains(x, y) && component.getClass() == TextComponent.class) {
				selectedTextArea = (TextComponent) component;
			}
		}
		if (selectedTextArea != null) {
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					if (server.availableForWriting(username, selectedTextArea)) {
						selectedTextArea.startWriting(this.username, this);
						componentInWriting = selectedTextArea;
						this.add(selectedTextArea);
						server.updateComponent(username, selectedTextArea);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				selectedTextArea.startWriting(this.username, this);
				componentInWriting = selectedTextArea;
				this.add(selectedTextArea);
			}

		}
		this.repaint();

	}

	public synchronized void changeModificationType(int x, int y) {
		if (componentInEdit != null) {
			componentInEdit.updateEditType(x, y);
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					server.updateComponent(username, componentInEdit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.repaint();
	}

	public synchronized void exitModificationType() {
		if (this.componentInEdit != null) {
			this.componentInEdit.resetEditType();
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					this.server.updateComponent(username, componentInEdit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.repaint();
	}

	public void updateSelectionMouseCursor(int x, int y) {
		if (componentInEdit != null) {
			componentInEdit.updateMouseCursor(this, x, y);
		} else {
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
	}

	public void updateDrawingMouseCursor(int x, int y) {
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	public synchronized void setColor(Color color) {
		this.color = color;
		if (componentInEdit != null) {
			componentInEdit.setColor(color);
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					server.updateComponent(username, componentInEdit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.repaint();
	}

	public Color getColor() {
		return color;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	public synchronized void setThickness(int thickness) {
		this.thickness = thickness;
		if (componentInEdit != null) {
			componentInEdit.setThickness(thickness);
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					server.updateComponent(username, componentInEdit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.repaint();
	}

	public void updateUsers(ArrayList<String> users) {
		this.window.updateUsers(users);
	}

	@Override
	public void add(JButton button) throws RemoteException {
		super.add(button);

	}

	@Override
	public void remove(JButton button) throws RemoteException {
		super.removeAll();
	}

	public synchronized void delete(DSComponent component) {
		if (componentInEdit == component) {
			components.remove(component);
			if (!offline) {
//				while (!availableForRefresh) {
//				}
				try {
					server.deleteComponent(username, componentInEdit);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			componentInEdit = null;
		}
		this.repaint();

	}

	@Override
	public synchronized void setComponents(ArrayList<DSComponent> components) {
		this.components = components;
		this.removeAll();
		for (DSComponent component : components) {
			if (component.equals(newComponent)) {
				this.newComponent = component;
			} else if (component.isInEdit() && component.getUsername().equals(username)) {
				this.componentInEdit = component;
				componentInEdit.updateButton(this);
			}
			if (component instanceof TextComponent) {
				this.add((JPanel) component);
				if (((TextComponent) component).isInWriting() && component.getUsername().equals(username)) {
					this.componentInWriting = component;
				}
			}
		}
		this.repaint();
	}

	@Override
	public synchronized void updateComponent(DSComponent componentUpdated) {
		for (DSComponent component : components) {
			if (component.equals(componentUpdated)) {
				if (component.equals(componentInEdit)) {
					componentInEdit = componentUpdated;
				}
				if (component.equals(newComponent)) {
					newComponent = componentUpdated;
				}
				if (component.equals(componentInWriting)) {
					componentInWriting = componentUpdated;
				}
				components.remove(component);
				components.add(componentUpdated);
				if (componentUpdated instanceof TextComponent) {
					this.add((JPanel) componentUpdated);
				}
				repaint();
				return;
			}
		}
	}

	@Override
	public synchronized void addComponent(DSComponent component) {
		components.add(component);
		repaint();
	}

	@Override
	public synchronized void deleteComponent(DSComponent componentUpdated) {
		for (DSComponent component : components) {
			if (component.equals(componentUpdated)) {
				if (component.equals(componentInEdit)) {
					componentInEdit = null;
				}
				if (component.equals(newComponent)) {
					newComponent = null;
				}
				if (component.equals(componentInWriting)) {
					componentInWriting = null;
				}
				components.remove(component);
				repaint();
				return;
			}
		}
	}

	@Override
	public void displayNewUser(String username) {
		this.window.displayNewUser(username);

	}

	@Override
	public void removeUser(String username) {
		this.window.removeUser(username);
	}

	@Override
	public void kickedOut(String message) throws RemoteException {
		if (message.equals("USER_KICKED_OUT")) {
			JOptionPane.showMessageDialog((Component) this.window, "You have been disconnected from the server. \n"
					+ "You can still modify the whiteboard, but the changes won't be shared online.");
			this.window.updateUsers(new ArrayList<String>(Arrays.asList("OFFLINE")));
		} else if (message.equals("MANAGER_EXIT")) {
			JOptionPane.showMessageDialog((Component) this.window, "The host exited the application. \n"
					+ "You can still modify the whiteboard, but the changes won't be shared online.");
			this.window.updateUsers(new ArrayList<String>(Arrays.asList("OFFLINE")));
		}
		this.offline = true;
		this.window.setOffline(true);

	}

	@Override
	public boolean authorizeNewConnection(String username) {
		return JOptionPane.showConfirmDialog((Component) this.window, username + " wants to join. Do you accept ?",
				"New connection request", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public synchronized DSComponent[] getDSComponents() {
		return this.components.toArray(new DSComponent[components.size()]);
	}
}