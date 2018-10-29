package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import datamodel.AnimalTrack;
import datamodel.TimePoint;

/**
 * The class which stores all the data for the project in fields, which prevents duplicate data to be recorded
 * @author Team Curlew
 *
 */
public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;

	public ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

	public Video getVideo() {
		return video;
	}


	/**
	 * This method returns the unassigned segment that contains a TimePoint (between
	 * startFrame and endFrame) that is closest to the given x,y location
	 * 
	 * @param x          - x coordinate to search near
	 * @param y          - y coordinate to search near
	 * @param startFrame - (inclusive)
	 * @param endFrame   - (inclusive)
	 * @return the unassigned segment (AnimalTrack) that contained the nearest point
	 *         within the given time interval, or *null* if there is NO unassigned
	 *         segment that contains any TimePoints within the given range.
	 */
	public AnimalTrack getNearestUnassignedSegment(double x, double y, int startFrame, int endFrame) {
		AnimalTrack closestTrack = null;
//		TimePoint closestPoint = null;
		double shortestDistance = Integer.MAX_VALUE;
		for (AnimalTrack segment : unassignedSegments) {
			List<TimePoint> validPoints = segment.getTimePointsWithinInterval(startFrame, endFrame);
			TimePoint point = getNearestPoint(validPoints, x, y);
			if (point != null) {	
				double testDistance = point.getDistanceTo(x, y);
				if (shortestDistance > testDistance) {
					closestTrack = segment;
					shortestDistance = testDistance;
				}
			}
		}
		return closestTrack;
	}
	
	/**
	 * Finds the nearest TimePoint and returns it
	 * @param points, List<Points> that are being checked
	 * @param x, double x value
	 * @param y, double y value
	 * @return the closest point found
	 */
	public TimePoint getNearestPoint(List<TimePoint> points, double x, double y) {
		TimePoint closestPoint = null;
		double shortestDistance = Integer.MAX_VALUE;
		for (TimePoint testPoint : points) {
			double testDistance = testPoint.getDistanceTo(x, y);
			if (shortestDistance > testDistance) {
				closestPoint = testPoint;
				shortestDistance = testDistance;
			}
		}
		return closestPoint;
	}
	

	/**
	 * Saves the project information to a file
	 * @param saveFile the File to be saved
	 * @throws FileNotFoundException prevents crash
	 */
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	
	/**
	 * Converts a Gson to Json through a String
	 * @return the Json String
	 */
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	/**
	 * Loads project data from a saved file
	 * @param loadFile the saved project data to be loaded
	 * @return the now loaded ProjectData
	 * @throws FileNotFoundException prevents crashing
	 */
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	
	
	/**
	 * Converts project data back from Json
	 * @param jsonText the file as a Json
	 * @return the ProjectData
	 * @throws FileNotFoundException prevents crashing 
	 */
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
}
