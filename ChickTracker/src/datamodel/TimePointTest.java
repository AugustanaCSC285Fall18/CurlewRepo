package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TimePointTest {

	@Test
	void testPoints() {
		List<TimePoint> test = new ArrayList<TimePoint>();
		test.add(new TimePoint(100,100,0));		
		test.add(new TimePoint(110,110,1));		
		test.add(new TimePoint(150,200,5));		
		assertEquals(0,test.get(0).getFrameNum());
		assertEquals(1,test.get(1).getFrameNum());
		assertEquals(5,test.get(2).getFrameNum());
	}

}
