package application;

import java.io.ByteArrayInputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import datamodel.Video;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

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
	private ImageView videoView;
	
	public CalibrationController(Video video, Canvas canvas, MainWindowController mwController, ImageView videoView) {
		this.video = video;
		this.canvas = canvas;
		this.mwController = mwController;
		this.videoView = videoView;
	}
	
	public void handleMousePressedSetOrigin(MouseEvent event) {
		double realX = event.getSceneX();
		double realY = event.getSceneY();
		
		video.setOrigin(realX, realY);
		System.out.println("Origin set at " + video.getOrigin().toString());
		GraphicsContext g = canvas.getGraphicsContext2D(); 
		g.setFill(Color.GOLD);
		g.fillOval(realX, realY-65, 5, 5);
		mwController.resetMouseModeAndButtons();		
	}
	
	public void resizeCanvas() {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
		video.setCurrentFrameNum(video.getEmptyFrameNum());
		Mat matImage = video.readFrame();
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", matImage, buffer);
		Image blankImage = new Image(new ByteArrayInputStream(buffer.toArray()));

		double aspectRatio = blankImage.getWidth() / blankImage.getHeight();
		double realWidth = Math.min(videoView.getScene().widthProperty().doubleValue(),
				videoView.getScene().widthProperty().doubleValue() * aspectRatio);
		double realHeight = Math.min(videoView.getScene().heightProperty().doubleValue(),
				videoView.getScene().heightProperty().doubleValue() / aspectRatio);

		canvas.setHeight(realHeight);
		canvas.setWidth(realWidth);
	}
	
	
}
