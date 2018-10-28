package edu.augustana.csc285.Curlew;

//import java.awt.Color;
import java.awt.event.MouseListener;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import analysis.Analysis;

import org.opencv.*;
import javafx.scene.control.*;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import autotracking.DetectedShape;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {

	@FXML
	private Canvas canvas;

	private GraphicsContext graphic;

	@FXML
	private Button originButton;
	@FXML
	private Button btnBrowse;
	@FXML
	private ImageView videoView;
	@FXML
	private Slider sliderVideoTime;
	@FXML
	private TextField textFieldCurFrameNum;

	@FXML
	private TextField textfieldStartFrame;
	@FXML
	private TextField textfieldEndFrame;
	@FXML
	private Button btnAutotrack;
	@FXML
	private ProgressBar progressAutoTrack;

	@FXML
	private MenuButton menuBtnAnimals;
	@FXML
	private Button btnAddAnimal;
	@FXML
	private Button btnRemoveAnimal;
	@FXML
	private Button btnStartManualTrack;
	@FXML
	private Button btnStopManualTrack;

	@FXML
	private Button btnArena;
	
	@FXML
	private Button btnJumpAhead;
	@FXML
	private Button btnJumpBack;

	@FXML
	private Button btnSetFrameNum;
	
	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };

	private AutoTracker autotracker;
	private ProjectData project;
	private Stage stage;
	private List<AnimalTrack> animalList;
	private ArrayList<String> animalIdList;
	private AnimalTrack currentAnimal;
	private boolean manualTrackActive;

	private CalibrationController calibController;

	@FXML
	public void initialize() {
		// FIXME: this quick loading of a specific file and specific settings
		// is for debugging purposes only, since there's no way to specify
		// the settings in the GUI right now...
		// loadVideo("/home/forrest/data/shara_chicks_tracking/sample1.mp4");
		loadVideo("S:/class/cs/285/sample_videos/sample1.mp4");
		project.getVideo().setxPixelsPerCm(6.5); // these are just rough estimates!
		project.getVideo().setyPixelsPerCm(6.7);

//		loadVideo("/home/forrest/data/shara_chicks_tracking/lowres/lowres2.avi");
		// loadVideo("S:/class/cs/285/sample_videos/lowres2.mp4");
//		project.getVideo().setXPixelsPerCm(5.5); //  these are just rough estimates!
//		project.getVideo().setYPixelsPerCm(5.5);

		graphic = canvas.getGraphicsContext2D();
		graphic.setFill(Color.BLACK);

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));

		animalList = new ArrayList<AnimalTrack>();
		animalIdList = new ArrayList<String>();
		menuBtnAnimals.getItems().clear();
		menuBtnAnimals.setText("Animal Select");
		btnStartManualTrack.setDisable(true);
		btnStopManualTrack.setDisable(true);

	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)

		// videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

		calibController.sizeCenterPanel();

	}

	public void resetMouseModeAndButtons() {
		canvas.setOnMousePressed(null);
		btnStartManualTrack.setDisable(false);
		originButton.setDisable(false);
		// re-enable other buttons too, involving calibration, etc?
	}

	@FXML
	public void handleBrowse() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			loadVideo(chosenFile.getPath());
		}
	}

	@FXML
	public void handleOriginButton() {

		// prevents user from placing more than one origin
		originButton.setDisable(true);

		// means that when the ImageView (videoView) is clicked, origin will be set to
		// the point where the press occurred.
		// https://stackoverflow.com/questions/25550518/add-eventhandler-to-imageview-contained-in-tilepane-contained-in-vbox
		canvas.setOnMousePressed(e -> calibController.handleMousePressedSetOrigin(e));

	}

	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			// Video video = project.getVideo();
			project.getVideo().setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
			project.getVideo().setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(project.getVideo());
			btnAutotrack.setText("CANCEL auto-tracking");
		} else {
			autotracker.cancelAnalysis();
			btnAutotrack.setText("Start auto-tracking");
		}

	}

	// this method will get called repeatedly by the Autotracker after it analyzes
	// each frame
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
			videoView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			sliderVideoTime.setValue(frameNumber);
			textFieldCurFrameNum.setText(String.format("%05d", frameNumber));
		});
	}

	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();
			calibController = new CalibrationController(video, canvas, this, videoView);

			sliderVideoTime.setMax(video.getTotalNumFrames() - 1);
			showFrameAt(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			GraphicsContext g = canvas.getGraphicsContext2D();
			videoView.setImage(curFrame);

			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			double scalingRatio = getImageScalingRatio();
			//g.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio,
			// curFrame.getHeight() * scalingRatio);

			drawAssignedAnimalTracks(g, scalingRatio, frameNum);
			drawUnassignedSegments(g, scalingRatio, frameNum);

		}
		textFieldCurFrameNum.setText(String.format("%05d", frameNum));
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});

	}

	// not sure if we're going to need this method but it's here
	// just in case, don't delete or it will cause errors -Riley
	@FXML
	public void handleMenuBtnAnimals() {

	}

	// this method handles the addAnimalBtn and the menuBtnAnimals
	// objects because it makes it easier if we don't have to have
	// another array of MenuItem objects. Even though it isn't
	// seen each menu item will have its own listener. -Riley
	@FXML
	public void handleBtnAddAnimal() {

		Object possibleAnimal = JOptionPane.showInputDialog(null, "Enter Animals's Name:", "Adding New Animal",
				JOptionPane.PLAIN_MESSAGE);
		if (possibleAnimal instanceof String) {
			String newAnimal = (String) possibleAnimal;
			if (newAnimal.length() >= 20) {
				newAnimal = JOptionPane.showInputDialog(null, "Name was too long. Enter Valid Animals's Name:",
						"Adding New Animal", JOptionPane.PLAIN_MESSAGE);
			} else if (animalIdList.contains(newAnimal)) {
				newAnimal = JOptionPane.showInputDialog(null, "ID already used. Enter Valid Animal Name:",
						"Adding New Animal", JOptionPane.ERROR_MESSAGE);
			}
			if (newAnimal.length() < 1) {
				newAnimal = "Animal " + (project.getTracks().size() + 1);
			}
			project.getTracks().add(new AnimalTrack(newAnimal));
			animalIdList.add(newAnimal);

			MenuItem newItem = new MenuItem(newAnimal);
			menuBtnAnimals.getItems().add(newItem);

			//this has to be .size() == 1 not .isEmpty() == false
			//otherwise every time a new animal is added it will
			//allow the startManualTrackBtn to be clicked
			if (menuBtnAnimals.getItems().size() == 1) {
				btnStartManualTrack.setDisable(false);
			}

			newItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					String name = newItem.getText();
					int index = animalIdList.indexOf(name);
					currentAnimal = project.getTracks().get(index);
					menuBtnAnimals.setText(name);
				}
			});
		}

	}

	@FXML
	public void handleBtnRemoveAnimal() {
		if (currentAnimal != null) {
			int selectedAnimalIndex = project.getTracks().indexOf(currentAnimal);
			menuBtnAnimals.getItems().remove(selectedAnimalIndex);
			menuBtnAnimals.setText(null);
			project.getTracks().remove(selectedAnimalIndex);
			animalIdList.remove(selectedAnimalIndex);
			currentAnimal = null;

			if (menuBtnAnimals.getItems().isEmpty() == true) {
				canvas.setOnMousePressed(null);
				btnStartManualTrack.setDisable(false);
				btnStopManualTrack.setDisable(true);
			} else {
				currentAnimal = project.getTracks().get(0);
				menuBtnAnimals.setText(currentAnimal.getId());
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select an animal to remove.", "WARNING",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// users have to press add point every single time
	// which is cumbersome so we will have to figure
	// out how to streamline this -Riley

	public void handleBtnStartManualTrack() {
		canvas.setOnMousePressed(e -> handleMousePressForTracking(e));
		btnStartManualTrack.setDisable(true);
		btnStopManualTrack.setDisable(false);
	}

	public void handleBtnStopManualTrack() {
		canvas.setOnMousePressed(null);
		btnStartManualTrack.setDisable(false);
		btnStopManualTrack.setDisable(true);
	}

	public void handleMousePressForTracking(MouseEvent event) {

		double scalingRatio = getImageScalingRatio();
		
		double unscaledX = event.getX() / scalingRatio;
		double unscaledY = event.getY() / scalingRatio;

		// TimePoint newTimePoint = new TimePoint(actualX, actualY,
		// project.getVideo().getCurrentFrameNum());
		// currentAnimal.add(newTimePoint);

		int currentFrame = project.getVideo().getCurrentFrameNum() - 1;
		int skipToFrame = project.getVideo().getCurrentFrameNum() + 32;

		if ((Integer.parseInt(textfieldStartFrame.getText()) < currentFrame
				&& Integer.parseInt(textfieldEndFrame.getText()) > currentFrame)
				&& !project.getUnassignedSegments().isEmpty()) {
//		TimePoint newTimePoint = new TimePoint(actualX, actualY, project.getVideo().getCurrentFrameNum());
			AnimalTrack closestAutoTrackSegment = project.getNearestUnassignedSegment(unscaledX, unscaledY, currentFrame,
					skipToFrame);
			List<TimePoint> closestPoints = closestAutoTrackSegment.getTimePointsWithinInterval(currentFrame,
					skipToFrame);
			if (!closestPoints.isEmpty()) {
				TimePoint closestPoint = project.getNearestPoint(closestPoints, unscaledX, unscaledY);
				if (closestPoint.getDistanceTo(unscaledX, unscaledY) < 50) {
					skipToFrame = closestAutoTrackSegment.getFinalTimePoint().getFrameNum();
					currentAnimal.add(closestAutoTrackSegment);
					System.out.println("Found AutoTrack Segment! " + closestAutoTrackSegment);
				} else {
					TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
					currentAnimal.add(newTimePoint);
				}
			} else {
				TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
				currentAnimal.add(newTimePoint);
			}
		} else {
			TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
			currentAnimal.add(newTimePoint);
		}

		if (skipToFrame < project.getVideo().getEndFrameNum()) {
			sliderVideoTime.setValue(skipToFrame);
			showFrameAt(skipToFrame);
		} else {
			showFrameAt(currentFrame);
		}

//		sliderVideoTime.setValue(skipToFrame);

	}

	public void handleArena() {
		btnArena.setDisable(true);
		btnStartManualTrack.setDisable(true);
		btnStopManualTrack.setDisable(true);
		JOptionPane.showMessageDialog(null,
				"Set the horizontal by clicking bottom left of box to bottom right");
		
		canvas.setOnMousePressed(e -> calibController.startHorizontalScaling(e));

	}

	private void drawAssignedAnimalTracks(GraphicsContext g, double scalingRatio, int frameNum) {

		for (int i = 0; i < project.getTracks().size(); i++) {
			AnimalTrack track = project.getTracks().get(i);
			Color trackColor = TRACK_COLORS[i % TRACK_COLORS.length];
			Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant

			g.setFill(trackPrevColor);
			// draw chick's recent trail from the last few seconds
			for (TimePoint prevPt : track.getTimePointsWithinInterval(frameNum - 120, frameNum)) {
				g.fillOval(prevPt.getX() * scalingRatio - 3, prevPt.getY() * scalingRatio - 3, 7, 7);
			}
			// draw the current point (if any) as a larger dot
			TimePoint currPt = track.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.setFill(trackColor);
				g.fillOval(currPt.getX() * scalingRatio - 7, currPt.getY() * scalingRatio - 7, 15, 15);
			}
		}
	}

	private void drawUnassignedSegments(GraphicsContext g, double scalingRatio, int frameNum) {
		
		for (AnimalTrack segment : project.getUnassignedSegments()) {

			g.setFill(Color.DARKGRAY);
			// draw this segments recent past & near future locations
			for (TimePoint prevPt : segment.getTimePointsWithinInterval(frameNum - 30, frameNum + 30)) {
				g.fillRect(prevPt.getX() * scalingRatio - 1, prevPt.getY() * scalingRatio - 1, 2, 2);
			}
			// draw the current point (if any) as a larger square
			TimePoint currPt = segment.getTimePointAtTime(frameNum);
			if (currPt != null) {
				g.fillRect(currPt.getX() * scalingRatio - 5, currPt.getY() * scalingRatio - 5, 11, 11);
			}
		}
	}
	
	/*
	 * It doesn't look like these methods are jumping and going back
	 * the same amount, but they are.
	 */
	public void handleBtnJumpAhead() {
		if (project.getVideo().getCurrentFrameNum() + 31 < project.getVideo().getEndFrameNum()) {
			sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() + 31);
			showFrameAt(project.getVideo().getCurrentFrameNum());
		} else {
			showFrameAt(project.getVideo().getCurrentFrameNum() - 1);
		}
	}
	
	/*
	 * It doesn't look like these methods are jumping and going back
	 * the same amount, but they are.
	 */
	public void handleBtnJumpBack() {
		sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() - 35);
		showFrameAt(project.getVideo().getCurrentFrameNum());
	}
	
	
	
	public void handleBtnSetFrameNum() {
		int newFrameNum = Integer.MAX_VALUE;
		boolean enteredNum = false;
		String input = JOptionPane.showInputDialog(null, "Enter desired Frame Number:", 
				"Set Frame Number", JOptionPane.PLAIN_MESSAGE);
		try {
			newFrameNum = Integer.parseInt(input);
			enteredNum = true;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Please enter only numbers.", 
					"Set Frame Number", JOptionPane.ERROR_MESSAGE);
		}
		
		 if (newFrameNum < 1) {
			JOptionPane.showMessageDialog(null, "Number needs to be at least 1.", 
					"Set Frame Number", JOptionPane.ERROR_MESSAGE);
			
		} else if (newFrameNum < project.getVideo().getEndFrameNum()) {
				sliderVideoTime.setValue(newFrameNum);
				showFrameAt(newFrameNum);
				
		} else if (enteredNum == true) {
			JOptionPane.showMessageDialog(null, "Number cannot be greater than the number of frames.", 
					"Set Frame Number", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	//Our tracked points are stored as un-scaled points,
	//so in order to get them out appropriately you 
	//to multiply them by this number.
	private double getImageScalingRatio() {
		double widthRatio = canvas.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = canvas.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}
	
	public void handleAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About Message");
		alert.setHeaderText(null);
		String names = "Team Curlew: Bryson Adcock, Chris Baker, Kathryn Clark, Riley Smith, Leo ;)"
				+ "\nProject Supervisor: Dr. Forrest Stonedahl";
		String acknowledgements = "\n\nLeo did basically everything for the project\nJust kidding. He literally did the whole thing.";
		String usedLibraries = "\nLibraries: OpenCV, JavaFX, JavaSwing, JSON, GSON";
		
		alert.setContentText(names + acknowledgements + usedLibraries);

		alert.showAndWait();
	}
	
	public void handleSave() throws IOException {
		Analysis.exportProject(project);
	}
	
}
