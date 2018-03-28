package xabello.webcrawler.persistence;

import java.io.Serializable;

/**
 * Modelo de datos de la tabla con el listado de proxys que se pueden usar para establecer conexión
 * 
 * @author Xavier Abelló
 *
 */
public class ProxyServerVO implements Serializable {
	private static final long serialVersionUID = 7523456444950991765L;

	/** Clave principal */
	private Integer id;
	
	/** Ip address del proxy */
	private String ip;
	
	/** Número de puerto de conexión al proxy */
	private Integer port;
	
	/** Nombre de usuario en caso de que el proxy requiera autenticacion */
	private String username;
	
	/** Password de usuario en caso de que el proxy requiera autenticacion */
	private String userpwd;
	
	/** Indica si el proxy se puede usar */
	private Boolean enabled;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserpwd() {
		return userpwd;
	}

	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	
}
