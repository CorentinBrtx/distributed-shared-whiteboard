package components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.Serializable;

import remote.IWhiteBoardClient;

public interface DSComponent extends Shape, Serializable {
	
	public final static int TOP_LEFT = 1;
	public final static int BOTTOM_RIGHT = 2;
	public final static int BOTTOM_LEFT = 3;
	public final static int TOP_RIGHT = 4;
	public final static int CENTER = 0;
	public final static int ERROR = -1;
	
	public void draw(Graphics2D g, String username);

	public void setEnd(int x, int y);
	
	public void startEditing(IWhiteBoardClient whiteBoard, String username);
	
	public void stopEditing(IWhiteBoardClient whiteBoard, String username);

	public void updateEditType(int x, int y);

	public void editShape(int x, int y);

	public void updateMouseCursor(IWhiteBoardClient whiteBoard, int x, int y);

	public void resetEditType();

	public void setColor(Color color);

	public int getThickness();

	public void setThickness(int thickness);

	public String getUsername();

	public boolean isInEdit();

	public void createButton();

	public void setUsername(String username);
	
	public void updateButton(IWhiteBoardClient whiteBoard);

}
