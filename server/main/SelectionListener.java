package main;
import java.awt.event.MouseEvent;

public class SelectionListener implements FullMouseListener {
	
	private WhiteBoard whiteBoard;
	

	public SelectionListener(WhiteBoard whiteBoard) {
		this.whiteBoard = whiteBoard;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			this.whiteBoard.selectText(e.getX(), e.getY());
		  }
		else {
			this.whiteBoard.selectShape(e.getX(), e.getY());
		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.whiteBoard.changeModificationType(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.whiteBoard.exitModificationType();

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
		this.whiteBoard.editShape(e.getX(), e.getY());

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.whiteBoard.updateSelectionMouseCursor(e.getX(), e.getY());

	}

}
