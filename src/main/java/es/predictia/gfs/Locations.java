package es.predictia.gfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ucar.nc2.dods.DODSNetcdfFile;

/**
 * Locations of NCEP's hight resolution GFS model data server
 * 
 * @author Max
 * 
 */
class Locations {

	private Locations(){}
	
	private static final String BASE_LOCATION = "http://nomads.ncep.noaa.gov:9090/dods/gfs_0p25/gfs${date}/gfs_0p25_${hour}z";

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);
	private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormat.forPattern("HH").withZone(DateTimeZone.UTC);
	private static final int[] HOURS = new int[]{18, 12, 6, 0};
	
	public static List<String> getLocations(int maximumLocationAgeInDays){
		List<String> locations = new ArrayList<String>();
		DateTime minDate = new DateTime().withZone(DateTimeZone.UTC).minusDays(maximumLocationAgeInDays);
		for(int daysDiff = 0; daysDiff < maximumLocationAgeInDays + 1; daysDiff++){
			for(int hour : HOURS){
				DateTime time = new DateTime().withZone(DateTimeZone.UTC).withMillisOfDay(0).minusDays(daysDiff).withHourOfDay(hour);
				if(time.isBeforeNow() && time.isAfter(minDate)){
					Map<String, String> valuesMap = new HashMap<String, String>();
					valuesMap.put("date", DATE_FORMATTER.print(time));
					valuesMap.put("hour", HOUR_FORMATTER.print(time));
					StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
					String url = DODSNetcdfFile.canonicalURL(substitutor.replace(BASE_LOCATION));
					locations.add(url);
				}
			}
		}
		return locations;
	}

	public static DateTime getCreationTime(String gfsLocation){
		return LocationToCreationTime.getInstance().apply(gfsLocation);
	}

}
