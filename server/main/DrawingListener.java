package main;
import java.awt.event.MouseEvent;

public class DrawingListener implements FullMouseListener {
	
	private WhiteBoard whiteBoard;
	
	public DrawingListener(WhiteBoard whiteBoard) {
		this.whiteBoard = whiteBoard;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.whiteBoard.createNewComponent(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.whiteBoard.completeNewComponent(e.getX(), e.getY());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.whiteBoard.editNewComponent(e.getX(), e.getY());
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.whiteBoard.updateDrawingMouseCursor(e.getX(), e.getY());
		
	}

}