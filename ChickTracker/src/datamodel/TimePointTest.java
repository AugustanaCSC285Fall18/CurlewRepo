package datamodel;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TimePointTest {

	List<TimePoint> createListOfTestTimePoints() {
		List<TimePoint> test = new ArrayList<TimePoint>();
		test.add(new TimePoint(100, 100, 0));
		test.add(new TimePoint(110, 110, 1));
		test.add(new TimePoint(150, 200, 5));
		return test;
	}

	@Test
	void testConstructor() {
		List<TimePoint> test = createListOfTestTimePoints();

		// tests x and y
		assertEquals(new Point(100, 100), test.get(0).getPointAWT());
		assertEquals(new Point(110, 110), test.get(1).getPointAWT());
		assertEquals(new Point(150, 200), test.get(2).getPointAWT());

		// tests frames
		assertEquals(0, test.get(0).getFrameNum());
		assertEquals(1, test.get(1).getFrameNum());
		assertEquals(5, test.get(2).getFrameNum());

	}

	@Test
	void testDifferenceInTime() {
		List<TimePoint> test = createListOfTestTimePoints();

		// tests difference between frame numbers
		assertEquals(1, test.get(1).compareTo(test.get(0)));
		assertEquals(5, test.get(2).compareTo(test.get(0)));
		assertEquals(4, test.get(2).compareTo(test.get(1)));
	}

	@Test
	void testSetPoints() {
		List<TimePoint> test = createListOfTestTimePoints();
		
		// tests positive sets
		test.get(0).setX(1000);
		test.get(0).setY(2);
		assertEquals(1000, test.get(0).getX());
		assertEquals(2, test.get(0).getY());
		
		// tests negative sets
		test.get(1).setX(-1000);
		test.get(1).setY(110);
		assertEquals(-1000, test.get(1).getX());
		assertEquals(110, test.get(1).getY());
		

		// tests same data sets
		test.get(2).setX(150);
		test.get(2).setY(200);
		assertEquals(150, test.get(2).getX());
		assertEquals(200, test.get(2).getY());
	}
	
	@Test
	void testDistanceBetweenPoints() {
		List<TimePoint> test = createListOfTestTimePoints();

		// tests difference between points
		assertEquals(Math.sqrt(200), test.get(0).getDistanceTo(test.get(1)));
		assertEquals(Math.sqrt(12500), test.get(0).getDistanceTo(test.get(2)));
		assertEquals(Math.sqrt(9700), test.get(1).getDistanceTo(test.get(2)));
	}

}
