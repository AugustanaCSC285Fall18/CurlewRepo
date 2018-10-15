package application;

import datamodel.Video;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * Class proposed by Dr. Stonedahl which would possibly keep MainWindowController cleaner by handling
 * calibration methods. 
 * @author christopherbaker15
 *
 */
public class CalibrationController {
	private Video video;
	private Canvas canvas;
	private MainWindowController mwController;
	
	public CalibrationController(Video video, Canvas canvas, MainWindowController mwController) {
		this.video = video;
		this.canvas = canvas;
		this.mwController = mwController;
	}
	
	public void handleMousePressedSetOrigin(MouseEvent event) {
		video.setOrigin(event.getSceneX(), event.getSceneY());
		System.out.println("Origin set at " + video.getOrigin().toString());
		GraphicsContext g = canvas.getGraphicsContext2D(); 
		g.fillOval(100, 100, 10, 10);
		mwController.resetMouseModeAndButtons();		
	}
	
	
}
