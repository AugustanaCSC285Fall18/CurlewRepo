package application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

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
 * Class proposed by Dr. Stonedahl which would possibly keep
 * MainWindowController cleaner by handling calibration methods.
 * 
 * @author christopherbaker15
 *
 */
public class CalibrationController {
	private Video video;
	private Canvas canvas;
	private MainWindowController mwController;
	private ImageView videoView;
	private double xOne;
	private double xTwo;
	private double yOne;
	private double yTwo;
	


	public CalibrationController(Video video, Canvas canvas, MainWindowController mwController, ImageView videoView) {
		this.video = video;
		this.canvas = canvas;
		this.mwController = mwController;
		this.videoView = videoView;
	}

	/**
	 * Uses mouse click to change your origin to your clicked point, and places a
	 * circle graphic
	 * 
	 * @param event Mouse click where you want a point
	 */
	public void handleMousePressedSetOrigin(MouseEvent event) {
		double realX = event.getX();
		double realY = event.getY();
		
		
		video.setOrigin(realX, realY);
		System.out.println("Origin set at " + video.getOrigin().toString());
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.GOLD);
		g.fillOval(realX - 2.5, realY - 2.5, 5, 5);
		mwController.resetMouseModeAndButtons();
	}

	/**
	 * Sizes the ImageView and Canvas objects, making sure to keep the Canvas and
	 * Image view the same size. Creates a Mat from the empty frame and creates a
	 * single image, then uses that image to size ImageView and canvas
	 */
	public void sizeCenterPanel() {
		videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
		video.setCurrentFrameNum(video.getEmptyFrameNum());
		Mat matImage = video.readFrame();
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".png", matImage, buffer);
		Image blankImage = new Image(new ByteArrayInputStream(buffer.toArray()));

		double aspectRatio = blankImage.getWidth() / blankImage.getHeight();
//		double realWidth = Math.min(videoView.getScene().widthProperty().doubleValue(),
//				videoView.getScene().widthProperty().doubleValue() * aspectRatio);
//		double realHeight = Math.min(videoView.getScene().heightProperty().doubleValue(),
//				videoView.getScene().heightProperty().doubleValue() / aspectRatio);

		canvas.setHeight(videoView.getScene().widthProperty().doubleValue() / aspectRatio);
		canvas.setWidth(videoView.getScene().widthProperty().doubleValue());
		

	}

	public void calibrateXScale() {
		double distance = Math.sqrt(Math.pow(yTwo-yOne, 2)+ Math.pow(xTwo-xOne, 2));
		video.setxPixelsPerCm(distance);
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setStroke(Color.GREEN);
		g.strokeLine(xOne, yOne, xTwo, yTwo);
		System.out.print("Entered xPixelsPerCm: " + video.getxPixelsPerCm());
	}
	
	public void getHorizontalOne(MouseEvent event) {
		
		xOne = event.getX();
		yOne = event.getY();
		canvas.setOnMousePressed(e -> getHorizontalTwo(e));
	}
	
	public void getHorizontalTwo(MouseEvent event) {
		xTwo = event.getX();
		yTwo = event.getY();	
		mwController.resetMouseModeAndButtons();
		calibrateXScale();
	}

}
