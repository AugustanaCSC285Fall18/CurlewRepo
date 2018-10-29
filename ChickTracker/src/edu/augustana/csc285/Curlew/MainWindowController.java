package edu.augustana.csc285.Curlew;

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
import java.util.Optional;

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
import javafx.scene.control.ButtonBar.ButtonData;
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
	private ArrayList<String> animalIdList;
	private AnimalTrack currentAnimal;
	private boolean manualTrackActive;
	private boolean projectAlreadyRunning = false;

	private CalibrationController calibController;

	@FXML
	public void initialize() throws FileNotFoundException {
		// FIXME: this quick loading of a specific file and specific settings
		// is for debugging purposes only, since there's no way to specify
		// the settings in the GUI right now...
		// loadVideo("/home/forrest/data/shara_chicks_tracking/sample1.mp4");
//		loadVideo("S:/class/cs/285/sample_videos/sample1.mp4");
//		project.getVideo().setxPixelsPerCm(6.5); // these are just rough estimates!
//		project.getVideo().setyPixelsPerCm(6.7);

//		loadVideo("/home/forrest/data/shara_chicks_tracking/lowres/lowres2.avi");
		// loadVideo("S:/class/cs/285/sample_videos/lowres2.mp4");
//		project.getVideo().setXPixelsPerCm(5.5); //  these are just rough estimates!
//		project.getVideo().setYPixelsPerCm(5.5);

		if (projectAlreadyRunning == false) {

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Animal Tracker Welcome Window");
			alert.setHeaderText("Welcome to Curlew's Animal Tracker!");
			alert.setContentText("Select an option.");
			alert.setGraphic(null);

			ButtonType buttonNewProject = new ButtonType("Create New Project");
			ButtonType buttonLoadProject = new ButtonType("Load Existing Project");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(buttonNewProject, buttonLoadProject, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonNewProject) {
				Alert chooseVideo = new Alert(AlertType.INFORMATION);
				chooseVideo.setTitle("Video Select");
				chooseVideo.setHeaderText("Select a video to begin your project with.");
				// chooseVideo.setContentText("I have a great message for you!");
				chooseVideo.setGraphic(null);

				chooseVideo.showAndWait();
				handleBrowse();

				// Creates some basic instructions for the user to read prior to seeing the
				// window.
				Alert startUpInstructions = new Alert(AlertType.INFORMATION);
				startUpInstructions.setTitle("Instructions for Tracking");
				startUpInstructions.setHeaderText(null);
				startUpInstructions.setContentText("Please set the arena bounds for your selected video.\n"
						+ "Then choose the start time when all chicks are visible and the end time when you"
						+ " would like to end tracking.\nThen click auto tracking once.");
				startUpInstructions.showAndWait();

				projectAlreadyRunning = true;
				
				toggleButtonsOff(true);
				btnArena.setDisable(false);
			} else if (result.get() == buttonLoadProject) {
				handleLoadProject();
			} else {
				// ... user chose CANCEL or closed the dialog
			}
		}

		graphic = canvas.getGraphicsContext2D();
		graphic.setFill(Color.BLACK);

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));

		animalIdList = new ArrayList<String>();
		menuBtnAnimals.getItems().clear();
		menuBtnAnimals.setText("Animal Select");

	}

	/**
	 * Initializes with the FXML stage, and also sizes the Canvas and ImageView
	 * objects to be identical
	 * 
	 * @param stage
	 */
	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)

		// videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

		calibController.sizeCenterPanel();

		// resizes the stage after canvas so that buttons show no matter what size of
		// video
		stage.setHeight(canvas.getHeight() + 250);
		
	}

	/**
	 * Re-enables buttons (if they should be re-enabled) and set canvas'
	 * mousePressed to null
	 */
	public void resetMouseModeAndButtons() {
		canvas.setOnMousePressed(null);

		// re-enable other buttons too, involving calibration, etc?
	}

	/**
	 * Allows user to choose the video that they are going to work on
	 */
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
	public void handleSaveProject() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		fileChooser.setInitialFileName("project.txt");
		File chosenFile = fileChooser.showSaveDialog(stage);
		try {
			project.saveToFile(chosenFile);
			Alert successfulSaveAlert = new Alert(AlertType.INFORMATION);
			successfulSaveAlert.setTitle("Saving Project");
			successfulSaveAlert.setHeaderText(null);
			successfulSaveAlert.setContentText("Your save to " + chosenFile.getName() + " was successful.");
			successfulSaveAlert.showAndWait();
		} catch (FileNotFoundException e) {
		}
	}

	/**
	 * Loads the project data from a previously worked on project
	 * 
	 * @throws FileNotFoundException
	 */
	@FXML
	public void handleLoadProject() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			project = ProjectData.loadFromFile(chosenFile);
			initialize();
		}
	}

	/**
	 * Allows user to set an origin point on their video, which will adjust the
	 * where the data references as the origin
	 */
	@FXML
	public void handleOriginButton() {

		// prevents user from placing more than one origin
		originButton.setDisable(true);

		// means that when the ImageView (videoView) is clicked, origin will be set to
		// the point where the press occurred.
		// https://stackoverflow.com/questions/25550518/add-eventhandler-to-imageview-contained-in-tilepane-contained-in-vbox
		canvas.setOnMousePressed(e -> calibController.handleMousePressedSetOrigin(e));

	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	@FXML
	public void handleStartAutotracking() throws InterruptedException {
		if (autotracker == null || !autotracker.isRunning()) {
			// Video video = project.getVideo();
			project.getVideo().setStartAutoTrackFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
			project.getVideo().setEndAutoTrackFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
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

	/**
	 * Loads the video currently stored in the project field
	 * 
	 * @param filePath
	 */
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

	/**
	 * ImageView displays chosen frame number and changes text field to show new
	 * frame number
	 * 
	 * @param frameNum
	 */
	public void showFrameAt(int frameNum) {
		if (autotracker == null || !autotracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			GraphicsContext g = canvas.getGraphicsContext2D();
			videoView.setImage(curFrame);

			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			double scalingRatio = getImageScalingRatio();
			// g.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio,
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

		// for (AnimalTrack track : trackedSegments) {
		// System.out.println(track);
		// System.out.println(" " + track.getPositions());
		// }
		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnAutotrack.setText("Start auto-tracking");
		});

		toggleButtonsOff(false);
		btnStopManualTrack.setDisable(true);
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
		String animalName = promptAnimalID();
		if (!animalName.equals("")) { // will return the empty String if user cancels the add
			project.getTracks().add(new AnimalTrack(animalName));
			animalIdList.add(animalName);

			MenuItem newItem = new MenuItem(animalName);
			menuBtnAnimals.getItems().add(newItem);
			// this has to be .size() == 1 not .isEmpty() == false
			// otherwise every time a new animal is added it will
			// allow the startManualTrackBtn to be clicked
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

	/**
	 * Makes sure the user selects a valid name for the animal to be added.
	 * 
	 * @return a valid animal name selected by the user or the empty String if the
	 *         user cancelled the window
	 */
	private String promptAnimalID() {
		boolean invalidID = true;
		String animalID = "";
		while (invalidID) {
			animalID = "";// resets the ID each time the user is prompted to reset the choice after a
							// warning message
			TextInputDialog addAnimalDialog = new TextInputDialog("Animal " + (project.getTracks().size() + 1));
			addAnimalDialog.setTitle("Adding new Animal");
			addAnimalDialog.setContentText("Enter Animal ID: ");
			Optional<String> result = addAnimalDialog.showAndWait();
			if (result.isPresent()) { // if the user clicked ok
				animalID = result.get();
			}
			if (animalID.length() >= 20) { // if the user gave a ID that exceeds the max character limit
				showAlertMessage(AlertType.WARNING, "WARNING", "Invalid Animal ID", "ID is too long.");
			} else if (animalIdList.contains(animalID)) { // if the user gave an ID that is already assigned to another
															// animal
				showAlertMessage(AlertType.WARNING, "WARNING", "Invalid Animal ID",
						"ID is already assigned to another animal.");
			} else { // user provided a valid ID or cancelled the window
				invalidID = false;
			}
		}
		return animalID;
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
			showAlertMessage(AlertType.ERROR, "WARNING", null, "Please select an animal to remove.");
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
		int currentFrame = project.getVideo().getCurrentFrameNum() - 1;
		// checks to make sure the click is between the chosen start and end frames
		if (currentAnimal == null) {
			showAlertMessage(AlertType.WARNING, "WARNING", null, "Select a chick to track.");
		} else {
			if (Integer.parseInt(textfieldStartFrame.getText()) <= currentFrame
					&& Integer.parseInt(textfieldEndFrame.getText()) >= currentFrame) {
				double scalingRatio = getImageScalingRatio();

				// user click locations
				double unscaledX = event.getX() / scalingRatio;
				double unscaledY = event.getY() / scalingRatio;

				// int currentFrame = project.getVideo().getCurrentFrameNum() - 1;
				int skipToFrame = project.getVideo().getCurrentFrameNum() + 32;

				// checks if the AutoTrack has run and if you are within the bounds of the
				// autotrack
				if (!project.getUnassignedSegments().isEmpty()) {
					// finds the closest AutoTrack segment and creates a list of the closest points
					// in that segment within plus or minus 5 frames of the current frame
					AnimalTrack closestAutoTrackSegment = project.getNearestUnassignedSegment(unscaledX, unscaledY,
							currentFrame, skipToFrame);
					List<TimePoint> closestPoints = new ArrayList<>();
					try {
						closestPoints = closestAutoTrackSegment.getTimePointsWithinInterval(currentFrame - 5,
								currentFrame + 5);
					} catch (NullPointerException e) {

					}
					// checks to make sure there is points in the list of closest points
					if (!closestPoints.isEmpty()) {

						// finds the TimePoint that is closest to the click location
						TimePoint closestPoint = project.getNearestPoint(closestPoints, unscaledX, unscaledY);

						// TimePoint closestPoint =
						// closestAutoTrackSegment.getTimePointAtTime(currentFrame);

						// Checks to see if that point is close enough to the click location
						if (closestPoint.getDistanceTo(unscaledX, unscaledY) < 10) { // if close enough,
							// sets the frame that will be moved to next to the end of the autotrack segment
							skipToFrame = closestAutoTrackSegment.getFinalTimePoint().getFrameNum() + 1;

							// adds the timepoints from the segment to the current animal
							currentAnimal.add(closestAutoTrackSegment);

							// removes that segment from the unassigned segments list
							project.getUnassignedSegments().remove(closestAutoTrackSegment);

						} else { // if not close enough, create a new TimePoint from the click location and add
									// it to the current animal
							TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
							currentAnimal.add(newTimePoint);
						}
					} else {// if there are no points in the list of close points, create a new TimePoint
							// from the click location and add it to the current animal
						TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
						currentAnimal.add(newTimePoint);
					}
				} else { // if the autotrack was never run or you are outside of the time bounds of
							// autotrack, create a new TimePoint from the click location and add it to the
							// current animal
					TimePoint newTimePoint = new TimePoint(unscaledX, unscaledY, currentFrame);
					currentAnimal.add(newTimePoint);
				}

				// if the frame that the video will be moved to is not past the last frame in
				// the video, moved the slider and shows the next frame.
				int endFrame = project.getVideo().getEndAutoTrackFrameNum();
				if (skipToFrame < endFrame) {
					sliderVideoTime.setValue(skipToFrame);
					showFrameAt(skipToFrame);
				} else { // if the frame that the video will be moved to is past the last frame in the
							// video, it does not move the video
					sliderVideoTime.setValue(endFrame);
					showFrameAt(endFrame);
					showAlertMessage(AlertType.WARNING, "ATTENTION", null, "You have reached the chosen end time.");
				}
			} else if (Integer.parseInt(textfieldStartFrame.getText()) > currentFrame) {
				showAlertMessage(AlertType.WARNING, "WARNING", null, "You are before the chosen start time.");
				// Moves the video to display the start frame
				sliderVideoTime.setValue(Integer.parseInt(textfieldStartFrame.getText()));
				showFrameAt(Integer.parseInt(textfieldStartFrame.getText()));
			} else {
				showAlertMessage(AlertType.WARNING, "WARNING", null, "You are after the chosen end time");
			}
		}
	}

	/**
	 * (Renamed from the calibration method) This method is triggered when the user
	 * clicks the arena button, and begins the arena creating process handled in the
	 * CalibrationController class
	 * 
	 */
	public void handleArena() {
		btnArena.setDisable(true);
		btnStartManualTrack.setDisable(true);
		btnStopManualTrack.setDisable(true);
		showAlertMessage(AlertType.INFORMATION, "Setting Arena Bounds", null, "Set the horizontal by clicking bottom left of box to bottom right");
		canvas.setOnMousePressed(e -> calibController.startHorizontalScaling(e));

		btnAutotrack.setDisable(false);
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
	 * It doesn't look like these methods are jumping and going back the same
	 * amount, but they are.
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
	 * It doesn't look like these methods are jumping and going back the same
	 * amount, but they are.
	 */
	public void handleBtnJumpBack() {
		sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() - 35);
		showFrameAt(project.getVideo().getCurrentFrameNum());
	}

	public void handleBtnSetFrameNum() {
		boolean invalidFrameNum = true;
		String contentText = "Enter desired time (in seconds)";
		int newFrameNum = project.getVideo().getCurrentFrameNum();
		while (invalidFrameNum) {
			boolean enteredNum = false;
			TextInputDialog frameSelectionDialog = new TextInputDialog(textfieldStartFrame.getText());
			frameSelectionDialog.setTitle("Set Time");
			frameSelectionDialog.setHeaderText(null);
			frameSelectionDialog.setContentText(contentText);
			Optional<String> result = frameSelectionDialog.showAndWait();
			String input = "";
			if (result.isPresent()) {
				input = result.get();
			}

			try {
				newFrameNum = Integer.parseInt(input);
				if (newFrameNum < 0) {
					contentText = "Number needs to be at least 0.";
				} else if (newFrameNum > project.getVideo().getEndFrameNum()) {
					contentText = "Number cannot be greater than the length of the video.";
				} else {
					invalidFrameNum = false;
				}
			} catch (NumberFormatException e) {
				contentText = "Please enter only integers.";
			}
		}
		sliderVideoTime.setValue(newFrameNum);
		showFrameAt(newFrameNum);
	}

	// Our tracked points are stored as un-scaled points,
	// so in order to get them out appropriately you
	// to multiply them by this number.
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

	public void handleExport() throws IOException {
		Analysis.exportProject(project, getImageScalingRatio());
	}

	public static void showAlertMessage(AlertType alertType, String title, String header, String contentText) {
		Alert noAnimalSelectedAlert = new Alert(alertType);
		noAnimalSelectedAlert.setTitle(title);
		noAnimalSelectedAlert.setHeaderText(header);
		noAnimalSelectedAlert.setContentText(contentText);
		noAnimalSelectedAlert.showAndWait();
	}
	
	private void toggleButtonsOff(boolean toggle) {
		btnStartManualTrack.setDisable(toggle);
		btnStopManualTrack.setDisable(toggle);
		btnAutotrack.setDisable(toggle);
		originButton.setDisable(toggle);
		menuBtnAnimals.setDisable(toggle);
		btnAddAnimal.setDisable(toggle);
		btnRemoveAnimal.setDisable(toggle);
		btnArena.setDisable(toggle);
	}
}
