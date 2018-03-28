package xabello.webcrawler.persistence;

import java.util.List;

import org.sql2o.Connection;

/**
 * Clase para gestionar los accesos a base de datos.
 * 
 * @author Xavier Abelló
 *
 */
public class ProxyServerDAO extends BaseDAO {


	/**
	 * Obtener una lista de resultados de proxys que esten habilitados.
	 *
	 * @param pMaxRows Numero de registros máximos que se pueden retornar. 
	 *   Si no se informa se usa el valor por defecto.
	 * 
	 * @return Lista de elementos
	 */
	public List<ProxyServerVO> listEnabledProxys(Integer pMaxRows) {
		
		Integer maxRows;
		if( pMaxRows != null ) {
			maxRows = pMaxRows;
		}
		else {
			maxRows = DEFAULT_MAX_LIST_RESULTS;
		}
		
		String sql =
		        "SELECT id, ip, port, username, userpwd, enabled " +
		        "FROM proxy_server where enabled = 1 order by id limit 0, :nrows";

		List<ProxyServerVO> result = null;
		
	    try(Connection con = sql2o.open()) {
	    	result = con.createQuery(sql)
	        		.addParameter("nrows", maxRows)
	        		.executeAndFetch(ProxyServerVO.class);
	    }
	    
	    return result;	    
	}
}
