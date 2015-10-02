package es.predictia.gfs;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import es.predictia.gfs.Locations;

public class GfsLocationsTest {

	@Test
	public void testLocations() throws Exception {
		List<String> locations = Locations.getLocations(1);
		Assert.assertFalse(locations.isEmpty());
		for(String location : locations){
			Assert.assertNotNull(Locations.getCreationTime(location));
		}
	}
	
}
