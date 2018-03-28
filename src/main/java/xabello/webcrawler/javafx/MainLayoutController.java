package xabello.webcrawler.javafx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xabello.webcrawler.ApplicationProperties;
import xabello.webcrawler.BasicCrawler;
import xabello.webcrawler.persistence.ProxyServerDAO;
import xabello.webcrawler.persistence.ProxyServerVO;
import xabello.webcrawler.persistence.ScrapedDataDAO;
import xabello.webcrawler.persistence.ScrapedDataVO;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Controler de la pagina MainLayout
 * 
 * @author xevi
 *
 */
public class MainLayoutController {

	
	/** Logger de la classe */
	private static final Logger logger = LoggerFactory.getLogger(MainLayoutController.class);

	/** Propiedades de la aplicacion */
	private Properties configPropeties;
	
	/** Boton de inicio de escaneo */
	@FXML
	private Button jxfButtonStart; 

	/** Boton de inicio de escaneo */
	@FXML
	private Button jxfButtonStop; 
	
	/** Boton para exportar datos en xls */
	@FXML
	private Button jxfButtonExportarXls;
	
	/** Input para introducir la url de escaneo */
	@FXML
	private TextField jfxUrlTextField;
	
	/** Mensaje de informacion que se muestra al usuario del estado de la aplicacion */
	@FXML
	private Label jfxLabelStatus;
	
	/** Campo para mostrar el numero de paginas escaneadas */
	@FXML
	private Text jfxPageCounter;
	
	/** Bloque de informacion del escaneo */
	@FXML
	private VBox jfxBoxInfo;
	
	/** Flag que indica una peticion para cancelar proceso */
	private boolean peticionCancelar;
	
	/** Flag que indica si el escaneo ha sido cancelado por el usuario */
	private Integer estadoEscaneo;
	
	public Integer getEstadoEscaneo() {
		return estadoEscaneo;
	}
	
	/** Parent stage */
	private Stage jfxStage;
	
	public Stage getJfxStage() {
		return jfxStage;
	}
	
	public void setJfxStage(Stage jfxStage) {
		this.jfxStage = jfxStage;
	}
	
	/*
	 * Posibles estados de escaneo
	 */
	public final static Integer ESTADOESCANEO_PARADO 		= 0;
	public final static Integer ESTADOESCANEO_PROCESANDO 	= 1;
	public final static Integer ESTADOESCANEO_FINALIZADO 	= 2;
	public final static Integer ESTADOESCANEO_CANCELADO 	= 3;
	public final static Integer ESTADOESCANEO_ERROR 		= 4;
	
	/** Codigo identificador cuando se produce un error */
	private String codigoError;
	
	/*
	 * Codigos de error definidos 
	 */
	
	/** Error de conexion mediante proxy */
	private final static String CODERR_PROXY = "PROXY_ERR";
	
	
	/** Controlador del crawler4j */
	private CrawlController crawlController;
	
	public CrawlController getCrawlController() {
		return crawlController;
	}
	
	private CrawlConfig crawlConfig;
	
	/** Instancia unica que hace referencia a la pantalla cargada */
	private static MainLayoutController singletonController;
	
	/**
	 * Obtener instancia al controlador de la pantalla cargado
	 * @return Instancia al MainLayoutController cargado en memoria.
	 */
	public static MainLayoutController getSingletonController() {
		return singletonController;
	}
	
	
	/*
	 * Instancias a capa de persistencia
	 */
	
	/** Instancia al DAO */
	private ProxyServerDAO daoProxyServer;
	
	/**
	 * Constructor por defecto
	 */
	public MainLayoutController() {
		daoProxyServer = new ProxyServerDAO();
	}
	
	@FXML
	private void initialize() {
		// Obtener popiedades de configuracion de la aplicacion
		configPropeties = ApplicationProperties.getInstance().getConfigPropeties();
		
		logger.info("Aplicacion iniciada");
		
		jfxUrlTextField.setText(configPropeties.getProperty("crawler.default_url_seed"));
		
		singletonController = this;
	}
	
	/**
	 * Crear nuevo controlador de crawler
	 * @param pItProxyServerList Coleccion de proxys
	 * @param pResume Continuar el escaneo anterior
	 */
	private void createCrawlController(Iterator<ProxyServerVO> pItProxyServerList, boolean pResume) {
		
		/*
		 * crawlStorageFolder is a folder where intermediate crawl data is
		 * stored.
		 */
		String crawlStorageFolder;
		crawlStorageFolder = configPropeties.getProperty("crawler.storage_folder", "./temp/");
		
		crawlConfig = new CrawlConfig();

		crawlConfig.setCrawlStorageFolder(crawlStorageFolder);

		// Intervalo entre escaneos
		crawlConfig.setPolitenessDelay(1000);

		// Profundidad de escaneo
		// Hay dos posibilidades:
		// La url base de campañas
		if( jfxUrlTextField.getText().contains("/campaign") ) {
			crawlConfig.setMaxDepthOfCrawling(2);
		}
		// La url de una campaá concreta
		else {
			crawlConfig.setMaxDepthOfCrawling(1);
		}
		
		// Numero máximo de paginas a escaneas (se supone que no va a haber mas de 200000)
		crawlConfig.setMaxPagesToFetch(200000);

		// No incluir ficheros binarios en el crawling
		crawlConfig.setIncludeBinaryContentInCrawling(false);

		// Se deshabilita la posibilidad de reanudar una ejecución interrumpida
		crawlConfig.setResumableCrawling(pResume);

		// Poner un user agent para que parezca un navegador Firefox
		crawlConfig.setUserAgentString("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
		
		AuthInfo authInfo1;
		try {
			String authUrl = configPropeties.getProperty("crawler.auth.url");
			String authUsername = configPropeties.getProperty("crawler.auth.username","xabe80@hotmail.com");
			String authPassword = configPropeties.getProperty("crawler.auth.password","aaaaaa");
			
			authInfo1 = new FormAuthInfo(authUsername, authPassword,
					authUrl, "LoginForm[email]", "LoginForm[password]");
			crawlConfig.addAuthInfo(authInfo1);
		} catch (MalformedURLException e) {
			logger.error("La url proporcionada para autenticar no es correcta: {}");
			throw new RuntimeException("La url proporcionada para autenticar no es correcta");
		}	    
		
		do {
			try {
				crawlController = null;
				
				// Si los proxys estan habilitados, establecer datos de conexion
				ProxyServerVO proxyServer = null;
				if( pItProxyServerList != null && pItProxyServerList.hasNext() ) {
					proxyServer = pItProxyServerList.next();
					
					crawlConfig.setProxyHost(proxyServer.getIp());
					crawlConfig.setProxyPort(proxyServer.getPort());
					if( proxyServer.getUsername() != null ) {
						crawlConfig.setProxyUsername(proxyServer.getUsername()); 
						crawlConfig.setProxyPassword(proxyServer.getUserpwd());
					}
					logger.info("Se va a usar el proxy: {}", proxyServer.getIp() + ":" + proxyServer.getPort());
				}
				else {
					logger.info("Se va a conectar directamente sin proxy.");
				}
				
				/*
				 * Instantiate the controller for this crawl.
				 */
				long timeMsIniFetcher = System.currentTimeMillis();
				PageFetcher pageFetcher = new PageFetcher(crawlConfig);
	
				// Comprovar que no haya un timeout al establecer conexión
				// Lo que significaria que no hay conexion posible con el servidor
				if( (System.currentTimeMillis() - timeMsIniFetcher) < (15000) ) {
					RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
					robotstxtConfig.setEnabled(false);
					RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
					crawlController = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);

					// Asignar raiz URL de escaneo
					crawlController.addSeed(jfxUrlTextField.getText());
				}
				else {
					if( proxyServer != null ) {
						logger.warn("Tiempo de conexión sobrepasado conectando al proxy: {}", 
								proxyServer.getIp() + ":" + proxyServer.getPort() );
					}
				}
			} catch(Exception e) {
				logger.debug("No se ha podido establecer conexion", e);
			}
		} while(crawlController == null 
				&& pItProxyServerList != null && pItProxyServerList.hasNext());
	}
	
	/**
	 * Metodo para iniciar el proceso de escaneo
	 */
	@FXML
	private void startCrawling() {
		
		// Comprovar que no haya ya un proceso en ejecucion
		if( ESTADOESCANEO_PROCESANDO.equals(estadoEscaneo) ) {
			throw new RuntimeException("No se permite iniciar dos procesos simultaneos");
		}
		estadoEscaneo = ESTADOESCANEO_PROCESANDO;
		jfxLabelStatus.setText("Escaneando...");
		
		// Reinicar codigo error
		codigoError = null;
		
		logger.info("Petición de nuevo escaneo");

		// Acceso a base de datos
		ScrapedDataDAO scrapedDataDAO = new ScrapedDataDAO();  
		scrapedDataDAO.deleteAll();
		

		// Thread para controlar el progreso de escaneo
		final Timeline watchTimer = new Timeline();
		
		watchTimer.getKeyFrames().add(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {

		    @Override
		    public void handle(ActionEvent event) {
				try {
					if( ESTADOESCANEO_PROCESANDO.equals(estadoEscaneo)) {
						jfxPageCounter.setText( Integer.toString( BasicCrawler.getCrawledPages().intValue() ));
					} else if(ESTADOESCANEO_ERROR.equals(estadoEscaneo) ) {
						if( CODERR_PROXY.equals(codigoError) ) {
							jfxLabelStatus.setText("No se ha podido establecer conexión con el proxy. Para más información consultar los logs.");
						}
						else {
							jfxLabelStatus.setText("Error (consultar logs)");
						}
						
						jxfButtonStop.setVisible(false);
						jxfButtonStop.setDisable(false);
						jxfButtonStart.setDisable(false);
						jxfButtonExportarXls.setDisable(false);
						jfxUrlTextField.setDisable(false);
						logger.info("Escaneo finalizado con errores. Codigo error: {}", codigoError);
						watchTimer.stop();
					} else if( ESTADOESCANEO_CANCELADO.equals(estadoEscaneo) ) {
						jfxLabelStatus.setText("Escaneo cancelado");
						jxfButtonStop.setVisible(false);
						jxfButtonStop.setDisable(false);
						jxfButtonStart.setDisable(false);
						jxfButtonExportarXls.setDisable(false);
						jfxUrlTextField.setDisable(false);
						logger.info("Escaneo cancelado");
						watchTimer.stop();
					} else if( ESTADOESCANEO_FINALIZADO.equals(estadoEscaneo) ) {
						jfxLabelStatus.setText("Escaneo finalizado");
						jxfButtonStop.setVisible(false);
						jxfButtonStop.setDisable(false);
						jxfButtonStart.setDisable(false);
						jxfButtonExportarXls.setDisable(false);
						jfxUrlTextField.setDisable(false);
						watchTimer.stop();
					} else {
						jfxLabelStatus.setText("Error (consultar logs)");
						logger.error("Estado inconsistente");
						estadoEscaneo = ESTADOESCANEO_ERROR;
						watchTimer.stop();
					}
				} catch(Exception e) {
					logger.error("Error", e);
				}
		    }
		}));
		
		watchTimer.setCycleCount(Timeline.INDEFINITE);
		watchTimer.play();
		
		try {
			
			// Se inicia en un threead nuevo ya que es una operacion sincrona
			Task<Void> threadCrawler = new Task<Void>() {

				@Override
				protected Void call() throws Exception {
					try {
					

						/*
						 * numberOfCrawlers shows the number of concurrent threads that should
						 * be initiated for crawling.
						 */
						int numberOfCrawlers;
						try {
							numberOfCrawlers = Integer.parseInt( configPropeties.getProperty("crawler.number_of_crawlers","2") );
						} catch(Exception e) {
							logger.error("El valor especificado en la propiedad 'crawler.number_of_crawlers' debe ser un número entero.");
							throw e;
						}
						

						// Usar proxy en caso que esté habilitado por configuracion
						// Iterator que contiene la lista de proxys
						Iterator<ProxyServerVO> itProxyServerList;
						if( configPropeties.getProperty("crawler.use_proxy", "false").equals("true") ) {
							List<ProxyServerVO> proxyServerList = daoProxyServer.listEnabledProxys(null);

							itProxyServerList = proxyServerList.iterator();
							// Si no se encuentran proxys enviar un error
							if( !itProxyServerList.hasNext() ) {
								logger.error("No se han encotrado proxys habilitados en base de datos "
										+ "para conectar. Con el parametro de configuracion crawler.use_proxy=true "
										+ " es obligatorio tener proxys habilitados en base de datos. "
										+ "Para solucionar el problema hay que deshabilitar este parámetro o definir proxys.");
								estadoEscaneo = ESTADOESCANEO_ERROR;
								codigoError = CODERR_PROXY;
								throw new RuntimeException();
							}
						}
						else {
							itProxyServerList = null;
						}
						
						/*
						 * Bucle para posibilitar los reintentos de conexión en caso de que se usen proxys.
						 * Si no está habilitado la conexión por proxys (crawler.use_proxy) va a conectar directamente.
						 * Si hay una lista de proxys va a intentar conectar a ellos siguiendo el orden establecido en base
						 * de datos.
						 * Si no hay proxys o no consigue conectar a ninguno de ellos intentará conectar directamente.
						 * Si no se puede conectar va a sacar un mensaje de error y lanzar una excepción. 
						 */
						createCrawlController(itProxyServerList, false);
						
						// Si no hay instancia al controller es que no se ha podido establecer conexión
						if( crawlController == null ) {
							if( itProxyServerList != null ) {
								logger.error("No se ha podido establecer conexión con el servidor mediante ninguno de los "
										+ "proxys definidos en base de datos. Para conectar sin proxy "
										+ "deshabilitar parametro crawler.use_proxy");
								codigoError = CODERR_PROXY;
							}
							else {
								logger.error("No se ha podido establecer conexión con el servidor");
							}
							estadoEscaneo = ESTADOESCANEO_ERROR;
							return null;
						}
						
						
						peticionCancelar = false;
						
						logger.info("Iniciando nuevo escaneo");
						
						BasicCrawler.getCrawledPages().set(0);
						
						do {
							// Iniciar proceso de escaneo
							crawlController.start(BasicCrawler.class, numberOfCrawlers);
	
							if( BasicCrawler.getGlobalStatus().get() != BasicCrawler.STATUS_ENDED
									&& !peticionCancelar ) {
								logger.warn("Problemas de conexión. Se intenta establecer una nueva conexión.");
								crawlConfig.setResumableCrawling(false);
								createCrawlController(itProxyServerList, true);
							}
							
						} while( crawlController != null && !peticionCancelar &&
								BasicCrawler.getGlobalStatus().get() != BasicCrawler.STATUS_ENDED );
						
						if( peticionCancelar ) {
							estadoEscaneo = ESTADOESCANEO_CANCELADO;
							peticionCancelar = false;
						}
						else if( BasicCrawler.getGlobalStatus().get() != BasicCrawler.STATUS_ENDED ){
							estadoEscaneo = ESTADOESCANEO_ERROR;
						}
						else {
							estadoEscaneo = ESTADOESCANEO_FINALIZADO;
						}
						
						logger.info("Escaneo finalizado");
					} catch (Exception e) {
						logger.error("Se ha producido un error durante el escaneo", e);
						estadoEscaneo = ESTADOESCANEO_ERROR;
					}
					return null;
				}
			};
			
			logger.info("Levantar nuevo thread de escaneo [INI]");
//			Platform.runLater(threadCrawler);
			new Thread(threadCrawler).start();
			logger.info("Levantar nuevo thread de escaneo [FIN]");
									
			jxfButtonStart.setDisable(true);
			jxfButtonExportarXls.setDisable(true);
			jfxUrlTextField.setDisable(true);
			jfxBoxInfo.setVisible(true);
			jxfButtonStop.setVisible(true);
			
		} catch (Exception e) {
			logger.error("No se ha podido iniciar el escaneo", e);
			jfxLabelStatus.setText("Error (consultar logs)");
		}
	}
	
	@FXML
	private void stopCrawling() {
		peticionCancelar = true;
		crawlController.shutdown();
		jxfButtonStop.setDisable(true);
		jfxLabelStatus.setText("Deteniendo procesos...");
	}	
	
	
	/**
	 * Accion del boton de exportar datos a XLS
	 */
	@FXML
	private void actionButtonExportarXls() {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Guardar fichero");
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("datasheet", "xls"));
		fileChooser.setInitialFileName("productos.xls");
		File path = fileChooser.showSaveDialog(jfxStage);
		exportarXls(path);
	}
	
	/**
	 * Funcion de exportacion de datos a xls
	 * 
	 * @param pPath Ubicacion donde se va a guardar el fichero
	 */
	private void exportarXls(File pPath) {

		
		try (HSSFWorkbook poiWorkbook = new HSSFWorkbook()){
			ScrapedDataDAO daoScrapedData = new ScrapedDataDAO();
			
			// Crear nueva hoja de cálculo
	        HSSFSheet poiSheet = poiWorkbook.createSheet("Lista productos");
			int sheetRowNum = 0;
			int rowColumn = 0;
			
	        // Crear cabecera
			HSSFRow poiRowHeader = poiSheet.createRow(sheetRowNum++);
			HSSFCellStyle poiHeaderStyle = poiWorkbook.createCellStyle();
			HSSFFont poiHeaderFontStyle = poiWorkbook.createFont();
			poiHeaderFontStyle.setBold(true);
			poiHeaderStyle.setFont(poiHeaderFontStyle);
			
			HSSFCell headerCell;
			poiSheet.setColumnWidth(rowColumn, 12000);
			headerCell = poiRowHeader.createCell(rowColumn++);
			headerCell.setCellValue("Nombre");
			headerCell.setCellStyle(poiHeaderStyle);
			poiSheet.setColumnWidth(rowColumn, 20000);
			headerCell = poiRowHeader.createCell(rowColumn++);
			headerCell.setCellValue("Descripción");
			headerCell.setCellStyle(poiHeaderStyle);
			poiSheet.setColumnWidth(rowColumn, 10000);
			headerCell = poiRowHeader.createCell(rowColumn++);
			headerCell.setCellValue("Imagen");
			headerCell.setCellStyle(poiHeaderStyle);
			poiSheet.setColumnWidth(rowColumn, 2000);
			headerCell = poiRowHeader.createCell(rowColumn++);
			headerCell.setCellValue("Precio");
			headerCell.setCellStyle(poiHeaderStyle);

			
			// Iterar productos en base de datos
			List<ScrapedDataVO> tempData;
			Integer batchRows = 500;
			Integer offest = 0;
			while( !(tempData = daoScrapedData.getList(offest, batchRows)).isEmpty() ) {
				rowColumn = 0;
				HSSFRow poiNewRow;
				
				// Cada iteración es una fila
				for( ScrapedDataVO scrapedData : tempData ) {
					poiNewRow = poiSheet.createRow(sheetRowNum++);
					rowColumn = 0;
					poiNewRow.createCell(rowColumn++).setCellValue(scrapedData.getNombre());
					poiNewRow.createCell(rowColumn++).setCellValue(scrapedData.getDescripcion());
					poiNewRow.createCell(rowColumn++).setCellValue(scrapedData.getImagen());
					poiNewRow.createCell(rowColumn++).setCellValue(scrapedData.getPrecio().doubleValue());
				}
				
				offest+=batchRows;
			}
			try (FileOutputStream out = 
                    new FileOutputStream(pPath)){
	            poiWorkbook.write(out);
	        } catch (FileNotFoundException e) {
				logger.error("Se ha producido un error durante la generación del xls", e);
	        } catch (IOException e) {
				logger.error("Se ha producido un error durante la generación del xls", e);
	        }
		} catch(Exception e) {
			logger.error("Se ha producido un error durante la generación del xls", e);
		}
	
	}
}
