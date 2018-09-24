
package datamodel;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;


public class AnimalTrack {
	public static final String UNAMED_ID = "<<unassigned>>";
	private String animalID = UNAMED_ID;
	
	private List<TimePoint> positions;
	private Color ptColor;
	
	public AnimalTrack() {
		positions = new ArrayList<TimePoint>();
	}
	
	public boolean hasIDAssigned() {
		return !animalID.equals(UNAMED_ID);
	}
	
	public List<TimePoint> getPositions() {
		return positions;
	}
	
	public Color getPtColor() {
		return ptColor;
	}

	@Override
	public String toString() {
		return "AnimalTrack[id=" + animalID + ",len=" + positions.size()+"]";
	}
	
}
