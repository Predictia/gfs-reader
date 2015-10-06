# gfs-reader

Library for accessing GFS's public OPeNDAP server

It's use is really simple. Just add the dependency to your project's POM:

```xml
<dependencies>
  <dependency>
    <groupId>es.predictia</groupId>
    <artifactId>gfsreader</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>
<repositories>
  <repository>
    <id>predictia-public-releases</id>
    <url>https://raw.githubusercontent.com/Predictia/maven-repo/master/releases</url>
  </repository>
</repositories>
```

And start accessing to the forecasts at your preferred location:

```java
import java.util.Map;
import org.joda.time.DateTime;
import es.predictia.gfs.GridName;
import es.predictia.gfs.TimeSeriesReader;

class MyReader {
	static Map<DateTime, Double> getRainSeries(Double lon, Double lat) {
		return TimeSeriesReader.readTimeSeries(GridName.apcpsfc, lon, lat);
	}
}
```

The full list of available grids is available in the [API docs](http://predictia.github.io/gfs-reader/apidocs/index.html).
