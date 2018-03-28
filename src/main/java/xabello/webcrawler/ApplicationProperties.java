package xabello.webcrawler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase que da acceso a las propiedades de configuracion de la aplicacion
 * @author xevi
 *
 */
public class ApplicationProperties implements Serializable {
	private static final long serialVersionUID = -8873375364388927469L;

	/** Logger de la classe */
	private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
	
	/** instancia estatica de la clase (modelo singleton) */
	private static ApplicationProperties _singleton;

	/** Propiedades de la aplicacion */
	private Properties configPropeties;
	
	/** Nombre de fichero de propiedades donde se encuentran los parametros de conexion a base de datos */
	private static String PROPETIES_FILENAME = "config.properties";
	
	/**
	 * No se permite acceder al constructor des del exterior.
	 * Usar getInstance
	 */
	private ApplicationProperties() {
		configPropeties = new Properties();
		
//		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPETIES_FILENAME);
		
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(PROPETIES_FILENAME);
		} catch (FileNotFoundException e1) {
			logger.info("No se encuentra fichero de configuracion en el directorio actual. Se usa configuracion por defecto");
			inputStream = getClass().getClassLoader().getResourceAsStream(PROPETIES_FILENAME);
		}
		
		try {
			configPropeties.load(inputStream);
		} catch (IOException e) {
			logger.error("Error cargando fichero de propiedades: " + PROPETIES_FILENAME);
			throw new RuntimeException();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error("Error de acceso a fichero", e);
			}
		}
	}
	
	/**
	 * Metodo para obtener instancia de la clase
	 * @return
	 */
	public static ApplicationProperties getInstance() {
		if( _singleton == null ) {
			_singleton = new ApplicationProperties();
		}
		return _singleton;
	}
	
	/**
	 * Obtener propiedades
	 * @return Propiedades de la aplicacion
	 */
	public Properties getConfigPropeties() {
		return configPropeties;
	}
	
}
