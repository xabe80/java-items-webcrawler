package xabello.webcrawler;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import xabello.webcrawler.javafx.MainLayoutController;
import xabello.webcrawler.persistence.ScrapedDataDAO;
import xabello.webcrawler.persistence.ScrapedDataVO;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Clase principal de crawler
 * Extiende de las librerias de Crawler4j
 * 
 * @author Xavier Abelló
 *
 */
public class BasicCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp3|zip|gz))$");
	
	private final static Pattern ITEM_PAGE_PATTERN = Pattern.compile(".*(\\.(html))$");
	
	/** Formateador para precios */
	private DecimalFormat priceDecimalFormat;

	/** Propiedades de la aplicacion */
	private Properties configPropeties;

	/** Item title DOM class */
	private String itemTitleClass;
	/** Item description DOM class */
	private String itemDescriptionClass;
	/** Item price DOM class */
	private String itemPrice1Class;
	/** Item image DOM id */
	private String itemImageId;

	/** 
	 * Esta variable indica si se ha cumplido la cadencia inicial. 
	 * Es necesario aplicar un tiempo de espera entre peticiones al principio
	 * para que se aplique un parámetro <i>desktop</i>. Se ha comprobado 
	 * que una vez aplicado este parametro ya se puede accelerar el tiempo entre peticiones
	 */
//	private static Boolean intervaloEspera = true;
	
	/*
	 * Posibles valores de la variable 
	 */
	
	public final static int STATUS_INITIAL 			= 0;
	public final static int STATUS_PROCESSING 		= 1;
	public final static int STATUS_ENDED	 		= 2;
	public final static int STATUS_FETCH_ERROR 		= 3;
	
	/** Estado global del proceso de escaneo */
	private static AtomicInteger globalStatus = new AtomicInteger(STATUS_INITIAL);
	
	/**
	 * Obtener el estado del escaneo
	 * @return Valor definido en los posibles estados de escaneo
	 */
	public static AtomicInteger getGlobalStatus() {
		return globalStatus;
	}
	
	public BasicCrawler() {
		priceDecimalFormat = new DecimalFormat();
		priceDecimalFormat.setParseBigDecimal(true);
		configPropeties = ApplicationProperties.getInstance().getConfigPropeties();

		itemTitleClass = configPropeties.getProperty("crawler.dom_mapping.title");
		itemDescriptionClass = configPropeties.getProperty("crawler.dom_mapping.description");
		itemPrice1Class = configPropeties.getProperty("crawler.dom_mapping.price");
		itemImageId = configPropeties.getProperty("crawler.dom_mapping.image");

		// Iniciar contador
//		if( crawledPages == null ) {
//			crawledPages = new AtomicInteger();
//			crawledPages.set(0);
//		}
	}
	
	/** Contador de numero de paginas escaneadas */
	private static AtomicInteger crawledPages = new AtomicInteger();
	
	/**
	 * Obtener numero de paginas escaneadas
	 * @return
	 */
	public static AtomicInteger getCrawledPages() {
		return crawledPages;
	}
	
	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();

		return (
					href.equals("http://www.crwlsample.com/campaign/") ||
					href.startsWith("http://www.crwlsample.com/c-") ||
					ITEM_PAGE_PATTERN.matcher(href).matches()
					
				)
				&& !href.contains("device=phone")
				&& !FILTERS.matcher(href).matches();
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		
		globalStatus.set(STATUS_PROCESSING);
		
		// Acceso a base de datos
		ScrapedDataDAO scrapedDataDAO = new ScrapedDataDAO();  
			
		// Espera entre peticiones
//		if( intervaloEspera ) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e1) {
//				logger.error("Error en sleep", e1);
//			}
//		}
//		
//		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
//		String domain = page.getWebURL().getDomain();
//		String path = page.getWebURL().getPath();
//		String subDomain = page.getWebURL().getSubDomain();
//		String parentUrl = page.getWebURL().getParentUrl();
//		String anchor = page.getWebURL().getAnchor();

//		logger.debug("Docid: {}", docid);
		logger.debug("URL: {}", url);
//		logger.debug("Domain: '{}'", domain);
//		logger.debug("Sub-domain: '{}'", subDomain);
//		logger.debug("Path: '{}'", path);
//		logger.debug("Parent page: {}", parentUrl);
//		logger.debug("Anchor text: {}", anchor);
		try {
			if (page.getParseData() instanceof HtmlParseData && ITEM_PAGE_PATTERN.matcher(url).matches()) {
				
				// Una vez se entra en un item ya se puede quitar el tiempo de espera
//				intervaloEspera = false;
				
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
//				String text = htmlParseData.getText();
	
				String html = htmlParseData.getHtml();
//				Set<WebURL> links = htmlParseData.getOutgoingUrls();
	
				// Parsear el HTML mediante el framework DOM de Jsoup
				Document doc = Jsoup.parseBodyFragment(html);
				
				// Elemento pagina: Titulo
				Elements elNombre = doc.getElementsByClass(itemTitleClass);
				
				ScrapedDataVO voScrapedData = new ScrapedDataVO();
				if( elNombre != null && elNombre.size() == 1 ){ 
					// Utilizar el titulo para detectar si estamos en la pagina de un producto
					voScrapedData.setNombre(elNombre.first().text());
					logger.debug("Titulo: {}", elNombre.first().text());
				}
	
				// Elemento pagina: Precio
				Elements elPrecio = doc.getElementsByClass(itemPrice1Class);
				try {
					voScrapedData.setPrecio((BigDecimal)priceDecimalFormat.parse(elPrecio.first().text()));
				} catch (ParseException e) {
					throw new RuntimeException("Error parseando el precio: " + elPrecio.first().text());
				}
				logger.debug("Precio: {}", elPrecio.first().text());

				// Elemento pagina: Descripcion
				Elements elDescripcion = doc.getElementsByClass(itemDescriptionClass);
				
				if( elDescripcion != null ){ 
					voScrapedData.setDescripcion(elDescripcion.first().text());
					logger.debug("Descripcion: {}", elDescripcion.first().text());
				}
				
				// Elemento pagina: Imagen
				Element elImagen = doc.getElementById(itemImageId);
				if( elImagen != null ) {
					voScrapedData.setImagen(elImagen.attr("src"));
					logger.debug("Imagen: {}", elImagen.attr("src"));
				}
	
				Element elCodigoProducto = doc.getElementById("p");
				if( elCodigoProducto != null ) {
					voScrapedData.setCodigoItem(elCodigoProducto.attr("value"));
					logger.debug("Codigo Item: {}", elCodigoProducto.attr("value"));
				}
				
				scrapedDataDAO.insert(voScrapedData);
				crawledPages.incrementAndGet();
			}
		} catch(Exception e) {
			logger.error("Error al procesar artículo: {}", url);
		}
	}
	
	@Override
	protected void onContentFetchError(WebURL webUrl) {
		super.onContentFetchError(webUrl);
		logger.error("No se ha podido obtener conexión con la página: " + webUrl.toString());
		globalStatus.set(STATUS_FETCH_ERROR);
		MainLayoutController.getSingletonController().getCrawlController().shutdown();
	}
	
	@Override
	protected void onUnexpectedStatusCode(String urlStr, int statusCode,
			String contentType, String description) {
		super.onUnexpectedStatusCode(urlStr, statusCode, contentType, description);
	}
	
	@Override
	protected void onParseError(WebURL webUrl) {
		super.onParseError(webUrl);
	}
	
	@Override
	public void onBeforeExit() {
		super.onBeforeExit();
		
		if( globalStatus.get() == STATUS_PROCESSING)
			globalStatus.set(STATUS_ENDED);
	}
}
