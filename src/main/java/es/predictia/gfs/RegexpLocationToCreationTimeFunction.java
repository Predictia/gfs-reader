package es.predictia.gfs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/** Obtiene la fecha de creacion a partir del nombre de la localizacion
 * @author Max
 *
 */
class RegexpLocationToCreationTimeFunction implements Function<String, DateTime>{

	private final String regExp, locationRegExpCreationDate;
	private final DateTimeFormatter fmt;
	private final Function<String, String> locationAdapter;
	
	/**
	 * @param locationRegExp, expresion regular del archivo, capturando grupos para la fecha de creacion
	 * @param locationRegExpCreationDate, por ejemplo $1$2$3
	 * @param locationRegExpCreationDateSDF, para definir el {@link DateTimeFormat} a patir de los grupos
	 * @throws IllegalArgumentException
	 */
	public RegexpLocationToCreationTimeFunction(String locationRegExp, String locationRegExpCreationDate, String locationRegExpCreationDateSDF) throws IllegalArgumentException {
		this(locationRegExp, locationRegExpCreationDate, locationRegExpCreationDateSDF, LocationAdapter.EMPTY);
	}
	
	/**
	 * @param locationRegExp, expresion regular del archivo, capturando grupos para la fecha de creacion
	 * @param locationRegExpCreationDate, por ejemplo $1$2$3
	 * @param locationRegExpCreationDateSDF, para definir el {@link DateTimeFormat} a patir de los grupos
	 * @param locationAdapter funcion de preproceso a los nombres antes de aplicar la expresion regular
	 * @throws IllegalArgumentException
	 */
	public RegexpLocationToCreationTimeFunction(String locationRegExp, String locationRegExpCreationDate, String locationRegExpCreationDateSDF, Function<String, String> locationAdapter) throws IllegalArgumentException {
		super();
		if(StringUtils.isEmpty(locationRegExp) || StringUtils.isEmpty(locationRegExpCreationDate) || StringUtils.isEmpty(locationRegExpCreationDateSDF)){
			throw new IllegalArgumentException("No regexp/dateString/SDF provided");
		}
		this.regExp = locationRegExp;
		this.locationRegExpCreationDate = locationRegExpCreationDate;
		this.fmt = DateTimeFormat.forPattern(locationRegExpCreationDateSDF).withZone(DateTimeZone.UTC);
		this.locationAdapter = locationAdapter;
	}

	@Override
	public DateTime apply(String location) {
		String fechaNombre = regExpReplace(locationAdapter.apply(location), regExp, locationRegExpCreationDate);
		try{
			return fmt.parseDateTime(fechaNombre);
		}catch (Exception e) {
			LOGGER.debug("Bad database file creation date format: " + e.getMessage());
			return null;
		}
	}
	
	private static String regExpReplace(String texto, String regExp, String replaceString){
		try{
			Pattern r = Pattern.compile(regExp);
			Matcher m = r.matcher(texto);
			return m.replaceAll(replaceString);
		}catch (Exception e) {
			LOGGER.debug("Unable to replace replaceString " + replaceString + " in regExp " + regExp + " for text " + texto);
			return texto;
		}
	}

	public enum LocationAdapter implements Function<String, String>{
		/** No realiza ninguna adaptacion de la localizacion
		 * @author Max
		 *
		 */
		EMPTY{
			@Override
			public String apply(String arg0) {
				return arg0;
			}
		}, 
		/** Extrae el nombre del archivo de una localizacion
		 * @author Max
		 *
		 */
		FILE_NAME{
			@Override 
			public String apply(String arg0) {
				return FilenameUtils.getBaseName(arg0);
			}
		};
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RegexpLocationToCreationTimeFunction.class);
	
}
