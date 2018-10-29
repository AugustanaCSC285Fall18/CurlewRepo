package edu.augustana.csc285.Curlew;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import datamodel.Video;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
	private ArrayList<Double> scaleCoords = new ArrayList<Double>();

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

	// ============================================================================
	/*
	 * Everything below this line should be changed if we have time. This code works
	 * but doesn't follow any of our coding guidelines.
	 * 
	 */

	/*
	 * This uses the ArrayList field to create the scale for the x values
	 */
	public void calibrateXScale() {

		// this ugly thing takes the values from our ArrayList and uses pythgorean's
		// theorem to find the distance between the points.
//		String numberCentimeters = JOptionPane.showInputDialog(null, "Enter distance chosen in centimeters", "Adding New Animal",
//				JOptionPane.PLAIN_MESSAGE);
		video.setxPixelsPerCm(calculateDistance() / 81);
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setStroke(Color.GREEN);
		g.strokeLine(scaleCoords.get(0), scaleCoords.get(1), scaleCoords.get(2), scaleCoords.get(3));
		scaleCoords.clear();
		System.out.print("Entered xPixelsPerCm: " + video.getxPixelsPerCm());
		JOptionPane.showMessageDialog(null, "Set the vertical bounds by clicking bottom right to top right");
		canvas.setOnMousePressed(e -> startVerticalScaling(e));
	}

	/**
	 * Uses the ArrayList field to create the scale for the y values
	 */
	public void calibrateYScale() {

		// this ugly thing takes the values from our ArrayList and uses pythgorean's
		// theorem to find the distance between the points.
//		String numberCentimeters = JOptionPane.showInputDialog(null, "Enter distance chosen in centimeters", "Adding New Animal",
//				JOptionPane.PLAIN_MESSAGE);
//		
		video.setyPixelsPerCm(calculateDistance() / 53);
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setStroke(Color.RED);
		g.strokeLine(scaleCoords.get(0), scaleCoords.get(1), scaleCoords.get(2), scaleCoords.get(3));
		scaleCoords.clear();
		System.out.println("Entered yPixelsPerCm: " + video.getyPixelsPerCm());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Autotracking");
		alert.setHeaderText("Please specify the part of the video you would like to work with.");
		alert.setContentText("Enter the beginning and end of the section.");

		alert.showAndWait();
	}

	/**
	 * Starts the calibration process and records x and y coordinates as doubles in
	 * the scaleCoords ArrayList
	 * 
	 * @param event MouseEvent that records where you press the mouse
	 */
	public void startHorizontalScaling(MouseEvent event) {

//		xOne = event.getX();
//		yOne = event.getY();
		scaleCoords.add(event.getX());
		scaleCoords.add(event.getY());

		canvas.setOnMousePressed(e -> endHorizontalScaling(e));
	}

	/**
	 * Ends the recording of points for the horizontal scale and sends the
	 * MouseEvent into the vertical recording methods
	 * 
	 * @param event MouseEvent mousePressed
	 */
	public void endHorizontalScaling(MouseEvent event) {
		scaleCoords.add(event.getX());
		scaleCoords.add(event.getY());
		System.out.println(scaleCoords.toString());
		mwController.resetMouseModeAndButtons();
		calibrateXScale();
	}

	/**
	 * Uses the mousePressed event to record x and y coordinates as doubles for the
	 * scaleCoords ArrayList
	 * 
	 * @param event MouseEvent mousePressed
	 */
	public void startVerticalScaling(MouseEvent event) {
		scaleCoords.add(event.getX());
		scaleCoords.add(event.getY());

		canvas.setOnMousePressed(e -> endVerticalScaling(e));
	}

	/**
	 * Ends the vertical scaling and starts the calibrate Y scale method after
	 * recording second vertical point
	 * 
	 * @param event MouseEvent mousePressed
	 */
	public void endVerticalScaling(MouseEvent event) {
		scaleCoords.add(event.getX());
		scaleCoords.add(event.getY());
		System.out.println(scaleCoords.toString());
		mwController.resetMouseModeAndButtons();
		calibrateYScale();
	}

	/**
	 * Uses the calibration class' ArrayList field to measure the distance between
	 * 
	 * @return distance between 2 points on canvas
	 */
	public double calculateDistance() {
		return Math.sqrt(Math.pow(scaleCoords.get(3) - scaleCoords.get(1), 2)
				+ Math.pow(scaleCoords.get(2) - scaleCoords.get(0), 2));
	}

}
