package es.predictia.gfs;

import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.predictia.gfs.TimeSeriesReader;
import es.predictia.gfs.GridName;

public class GfsLocationReaderTest {

	@Test
	public void testLocations() throws Exception {
		String location = Locations.getLocations(1).get(1);
		Map<DateTime, Double> data = TimeSeriesReader.readTimeSeries(location, GridName.tmpsfc, -3.703790, 40.416775);
		Assert.assertFalse(data.isEmpty());
		for(Map.Entry<DateTime, Double> entry : data.entrySet()){
			LOGGER.debug("Temperature value for {}: {}", entry.getKey(), entry.getValue());
		}
	}
	
	@Test
	public void testLastLocation() throws Exception {
		Map<DateTime, Double> data = TimeSeriesReader.readTimeSeries(GridName.tmpsfc, -3.703790, 40.416775);
		Assert.assertFalse(data.isEmpty());
		for(Map.Entry<DateTime, Double> entry : data.entrySet()){
			LOGGER.debug("Temperature value for {}: {}", entry.getKey(), entry.getValue());
		}
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GfsLocationReaderTest.class);
	
}
