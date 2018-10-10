package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AnimalTrackTest {

	AnimalTrack makeNewAnimal(String name) {
		return new AnimalTrack(name);
	}
	
	@Test
	void testChickNaming() {
		AnimalTrack testTrack = makeNewAnimal("Clown-face");
		assertEquals("Clown-face",testTrack.getId());
	}
	
	
	@Test
	void testAddingAndGettingPoints() {
		AnimalTrack testTrack = makeNewAnimal("bill");
		
		testTrack.add(new TimePoint(100,100,0));		
		testTrack.add(new TimePoint(110,110,1));		
		testTrack.add(new TimePoint(150,200,5));		
		assertEquals(3,testTrack.getNumPoints());
		
		TimePoint ptAtZero = testTrack.getTimePointAtTime(0);
		assertEquals(new TimePoint(100,100,0), ptAtZero);
		TimePoint ptAt2 = testTrack.getTimePointAtTime(2);
		assertNull(ptAt2);
		TimePoint lastPt = testTrack.getFinalTimePoint();
		assertEquals(5,lastPt.getFrameNum());
	}

}
