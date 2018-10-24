package application;

//import java.awt.Color;
import java.awt.event.MouseListener;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private Button btnStartManualTrack;
	@FXML
	private Button btnStopManualTrack;

	@FXML
	private Button btnPlay;
	@FXML
	private Button btnPause;

	private AutoTracker autotracker;
	private ProjectData project;
	private Stage stage;
	private ArrayList<AnimalTrack> animalList;
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
		canvas.setOnMousePressed(e -> handleMousePressForTracking(e)); // switch back to manual tracking mode
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
			videoView.setImage(curFrame);
			textFieldCurFrameNum.setText(String.format("%05d", frameNum));

		}
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
		String newAnimal = JOptionPane.showInputDialog(null, "Enter Animals's Name:", "Adding New Animal", JOptionPane.PLAIN_MESSAGE);;
		if (newAnimal.length()<1) {
			System.out.println("Animals " + (animalList.size()+1));
			newAnimal = "Animals " + (animalList.size()+1);
		}
		animalList.add(new AnimalTrack(newAnimal));

		MenuItem newItem = new MenuItem(newAnimal);
		menuBtnAnimals.getItems().add(newItem);
		
		if (menuBtnAnimals.getItems().isEmpty() == false) {
			btnStartManualTrack.setDisable(false);
			
		}

		newItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				String name = newItem.getText();
				//AnimalTrack selectedAnimal = null;
				for (AnimalTrack animal : animalList) {
					if (animal.getId().equals(name)) {
						currentAnimal = animal;
					}
				}
				menuBtnAnimals.setText(name);
				System.out.println(currentAnimal);
			}
		});

	}

	
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
		double actualX = event.getSceneX() - project.getVideo().getOrigin().getX();
		double actualY = event.getSceneY() - project.getVideo().getOrigin().getY();

		TimePoint newTimePoint = new TimePoint(actualX, actualY, project.getVideo().getCurrentFrameNum());
		currentAnimal.add(newTimePoint);
		System.out.println("Current animal " + currentAnimal + actualX + ", " + actualY);
		graphic.setFill(Color.GREENYELLOW);
		graphic.fillOval(event.getX() - 5, event.getY() - 5, 10, 10);
		
		sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum() + 33);
	}

}
