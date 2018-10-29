package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import javafx.stage.FileChooser;

public class Analysis {

	public static void exportProject(ProjectData project, Double scalingRatio) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		String filePath = project.getVideo().getFilePath();
		filePath = filePath.substring(0, filePath.lastIndexOf("."));
		System.out.println(filePath);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		fileChooser.setInitialFileName("projectdata.csv");
		File chosenFile = fileChooser.showSaveDialog(null);
		FileWriter writer = new FileWriter(chosenFile);

		StringBuilder s = new StringBuilder();

		for (AnimalTrack animal : project.getTracks()) {
			s.append(animal.getId());
			s.append(",,,,");
		}

		s.append("\n");

		for (@SuppressWarnings("unused") AnimalTrack animal : project.getTracks()) {
			s.append("Seconds,X Coordinate, Y Coordinate,,");
		}

		s.append("\n");

		double totalNumSeconds = project.getVideo()
				.convertFrameNumsToSeconds(project.getVideo().getEndAutoTrackFrameNum());

		System.out.println(totalNumSeconds);
		double numFramesPerSecond = project.getVideo().getFrameRate();

		int secondNumStart = (int) project.getVideo()
				.convertFrameNumsToSeconds((project.getVideo().getStartAutroTrackFrameNum()));

		// goes through every second of tracked data and displays
		// the x y coordinates of the point each chick had that
		// was closest to that time
		for (int seconds = secondNumStart; seconds < totalNumSeconds; seconds++) {
			int frameNum = (int) Math.round((seconds * numFramesPerSecond));

			for (AnimalTrack animal : project.getTracks()) {
				TimePoint point = TimePoint.closestPointToFrame(animal, frameNum);
				System.out.println(point);
				if (point != null) {
					s.append(seconds);
					s.append(",");
					s.append(point.getX() * scalingRatio);
					s.append(",");
					s.append(point.getY() * scalingRatio);
					s.append(",,");
				} else {
					s.append(",,,,");
				}
			}
			s.append("\n");
		}

		writer.append(s);
		writer.close();
	}

}
