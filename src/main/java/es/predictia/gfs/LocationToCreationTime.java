package es.predictia.gfs;

class LocationToCreationTime extends RegexpLocationToCreationTimeFunction {

	private LocationToCreationTime() throws IllegalArgumentException {
		super(".+gfs([0-9]+)/gfs_0p25_([0-9]+)z", "$1$2", "yyyyMMddHH");
	}

	private static LocationToCreationTime INSTANCE = new LocationToCreationTime();

    public static LocationToCreationTime getInstance() {
        return INSTANCE;
    }
	
}