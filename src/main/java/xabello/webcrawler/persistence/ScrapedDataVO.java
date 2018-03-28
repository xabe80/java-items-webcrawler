package xabello.webcrawler.persistence;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Modelo de datos de la tabla principal de datos extraidos de cada producto
 * 
 * @author Xavier Abelló
 *
 */
public class ScrapedDataVO implements Serializable {
	private static final long serialVersionUID = -4402334087567098767L;

	/** Clave principal */
	private Integer id; 
	
	/** Codigo del producto extraido de la pagina original */
	private String codigoItem;
	
	/** Nombre del producto */
	private String nombre;
	
	/** Descripcion del producto */
	private String descripcion;
	
	/** Ubicacion de la imagen del producto */
	private String imagen;
	
	/** Precio del producto */
	private BigDecimal precio;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodigoItem() {
		return codigoItem;
	}

	public void setCodigoItem(String codigoItem) {
		this.codigoItem = codigoItem;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
		
}
