package xabello.webcrawler.persistence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;


/**
 * Clase para gestionar los accesos a base de datos.
 * 
 * @author Xavier Abelló
 *
 */
public class ScrapedDataDAO extends BaseDAO {

	/** Class logger */
	private static final Logger logger = LoggerFactory.getLogger(ScrapedDataDAO.class);
	

	/**
	 * Insertar registro
	 * @param pVo Datos a insertar
	 */
	public void insert(ScrapedDataVO pVo) {
		
		String insertSql = 
				"insert into scraped_data (codigo_item, nombre, descripcion, imagen, precio) " +
				"values (:codigoItem, :nombre, :descripcion, :imagen, :precio)";

		try (Connection con = sql2o.open()) {
		    con.createQuery(insertSql).bind(pVo).executeUpdate();
		} catch(Exception e) {
			logger.error("Error al insertar registro", e);
			throw e;
		}
		
	}
	
	/**
	 * Obtener registro mediante su código de producto 
	 * 
	 * @param pCodigoItem Codigo del producto
	 * @return 
	 */
	public ScrapedDataVO getByCodigoItem(String pCodigoItem) {
		String sql =
		        "SELECT id, codigo_item, nombre, descripcion, imagen, precio " +
		        "FROM scraped_data where codigo_item = :codigoItem";

		ScrapedDataVO result = null;
		
	    try(Connection con = sql2o.open()) {
	    	List<ScrapedDataVO> queryResult = con.createQuery(sql)
	        		.addParameter("codigoItem", pCodigoItem)
	        		.executeAndFetch(ScrapedDataVO.class);
	    	if( queryResult != null && queryResult.size() == 1) {
	    		result = queryResult.get(0);
	    	}
	    }
	    
	    return result;	    
	}
	
	
	/**
	 * Obtener una lista de resultados de la tabla paginados 
	 *
	 * @param pOffest Fila inicial (no null)
	 * @param pNrows Numero de registros obtenidos (no null)
	 * 
	 * @return Lista de elementos
	 */
	public List<ScrapedDataVO> getList(Integer pOffest, Integer pNrows) {
		
		if( pOffest == null || pNrows == null ) {
			throw new RuntimeException("Es obligatorio informar los parametros pOffest y pNrows");
		}
		
		String sql =
		        "SELECT id, codigo_item codigoitem, nombre, descripcion, imagen, precio " +
		        "FROM scraped_data order by id limit :offest, :nrows";

		List<ScrapedDataVO> result = null;
		
	    try(Connection con = sql2o.open()) {
	    	result = con.createQuery(sql)
	        		.addParameter("offest", pOffest)
	        		.addParameter("nrows", pNrows)
	        		.executeAndFetch(ScrapedDataVO.class);
	    }
	    
	    return result;	    
	}
	
	
	/**
	 * Delete all data from table
	 */
	public void deleteAll() {
		String insertSql = 
				"truncate table scraped_data";

		try (Connection con = sql2o.open()) {
		    con.createQuery(insertSql).executeUpdate();
		} catch(Exception e) {
			logger.error("Error al eliminar datos de la tabla", e);
			throw e;
		}
	}
	
}
