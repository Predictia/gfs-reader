package es.predictia.gfs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public class TimeSeriesReader {

	private TimeSeriesReader(){}
	
	/**
	 * Reads last available time series for requested location, with first z
	 * index. May return an empty map if errors occurred while reading the
	 * remote server.
	 */
	public static Map<DateTime, Double> readTimeSeries(GridName gridName, Double lon, Double lat){
		if((gridName == null) || (lon == null) || (lat == null)) throw new NullPointerException();
		LOGGER.info("Fetching timeSeries for {} at ({}, {})", gridName, lon, lat);
		TreeMap<DateTime, String> locationsMap = new TreeMap<DateTime, String>(FluentIterable
			.from(Locations.getLocations(1))
			.uniqueIndex(LocationToCreationTime.getInstance())
		);
		for(DateTime time : locationsMap.descendingKeySet()){
			String location = locationsMap.get(time);
			try{
				return readTimeSeries(location, gridName, lon, lat);
			}catch(IOException e){
				LOGGER.debug("Unable to get prediction created on {}: {}", time, e.getMessage());
			}
		}
		return Collections.emptyMap();
	}
	
	/** Shortcut of {@link #readTimeSeries(String, String, Double, Double, int)} with first z index
	 */
	static Map<DateTime, Double> readTimeSeries(String location, GridName gridName, Double lon, Double lat) throws IOException{
		return readTimeSeries(location, gridName, lon, lat, 0);
	}
	
	/**
	 * @param location GFS openDAP URL to read from, for example: dods://nomads.ncep.noaa.gov:9090/dods/gfs_0p25/gfs20151002/gfs_0p25_06z 
	 * @param gridName
	 * @param lon longitude 
	 * @param lat latitude
	 * @param zIndex starting with 0
	 * @return
	 * @throws IOException
	 */
	static Map<DateTime, Double> readTimeSeries(String location, GridName gridName, Double lon, Double lat, int zIndex) throws IOException{
		Map<DateTime, Double> data = new TreeMap<DateTime, Double>();
		
		GridDataset dataset = null;
		try{
			Stopwatch sw = Stopwatch.createStarted();
			dataset = GridDataset.open(location);
			
			GeoGrid grid = dataset.findGridByName(gridName.name());
			GridCoordSystem xyAxis = grid.getCoordinateSystem();
			CoordinateAxis zAxis = xyAxis.getVerticalAxis();
			
			int[] yxIndex = xyAxis.findXYindexFromLatLon(lat,lon,null);
			if(yxIndex.length==2){
				CoordinateAxis tAxis = xyAxis.getTimeAxis();
				List<CalendarDate> dates = Collections.emptyList();
				if(tAxis instanceof CoordinateAxis1DTime){
					dates = ((CoordinateAxis1DTime) tAxis).getCalendarDates();
				}
				Array tValues = tAxis.read();
				Long zValuesLength = 1l;
				if(zAxis != null){
					Array zValues = zAxis.read();
					zValuesLength = zValues.getSize();
				}
				for(int z=0;z<zValuesLength;z++){
					if(zIndex != z) continue;
					for(int t=0;t<tValues.getSize();t++){
						if(t >= dates.size()){
							continue;
						}
						Double val = Double.NaN;
						if(yxIndex[0]>=0 && yxIndex[1]>=0){
							Array dathum = grid.readDataSlice(t,z,yxIndex[1],yxIndex[0]);
							if(dathum != null){
								if(dathum.getSize()>0){
									val = dathum.getDouble(0);
								}
							}
						}
						DateTime time = new DateTime(dates.get(t).getMillis(), DateTimeZone.UTC); 
						data.put(time,Double.isNaN(val) ? null : val);
					}
				}
			}
			LOGGER.debug("Temporal series from location {} read in {}", location, sw);
		}finally{
			silentlyClose(dataset);
		}		
	    return ImmutableMap.copyOf(data);
	}
	
	private static void silentlyClose(GridDataset dataset){
		if(null != dataset){
			try{
				dataset.close();
			}catch(IOException e){
		        LOGGER.error("Error trying to close dataset {}: {}", dataset, e);
			}
		}
	}
	
	private final static Logger LOGGER = LoggerFactory.getLogger(TimeSeriesReader.class);

}
