package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.core.Core;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import javafx.stage.FileChooser;

public class Analysis {

	public static void exportProject(ProjectData project) throws IOException {
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
		
		for (AnimalTrack animal : project.getTracks()) {
			s.append("Seconds,X Coordinate, Y Coordinate,,");
		}
		
		s.append("\n");
		
		for (int index = 0; index < project.getTracks().get(0).getNumPoints(); index++) {
			for (AnimalTrack animal : project.getTracks()) {
				if (animal.getTimePointAtIndex(index) != null) {
					s.append(animal.getTimePointAtIndex(index).getFrameNum());
					s.append(",");
					s.append(animal.getTimePointAtIndex(index).getX());
					s.append(",");
					s.append(animal.getTimePointAtIndex(index).getY());
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
