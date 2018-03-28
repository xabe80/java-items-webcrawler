package xabello.webcrawler.persistence;

import java.util.Properties;

import org.sql2o.Sql2o;

import xabello.webcrawler.ApplicationProperties;

/**
 * Clase para implementar instancias de DAO
 * 
 * @author Xavier Abelló
 *
 */
public abstract class BaseDAO {

	/** Valor por defecto para el numero de resultados máximo de las consultas */
	protected final static Integer DEFAULT_MAX_LIST_RESULTS = 5000;
	
	
	/** Libreria de conexión a base de datos */
	protected Sql2o sql2o;
	
	/**
	 * Constructor principal
	 */
	public BaseDAO() {
		
		// Inicializar libreria de conexión a base de datos
		
		Properties configPropeties = ApplicationProperties.getInstance().getConfigPropeties();
		
		String dbUrl = configPropeties.getProperty("persistence.db.url");
		String dbUser = configPropeties.getProperty("persistence.db.user");
		String dbPass = configPropeties.getProperty("persistence.db.pass");
		
		sql2o = new Sql2o(dbUrl, dbUser, dbPass);
		
	}
}
