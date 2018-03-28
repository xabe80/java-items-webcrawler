package xabello.webcrawler;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xabello.webcrawler.javafx.MainLayoutController;


/**
 * Clase principal de la aplicacion
 * 
 * Al llamar el main se inicia la interfaz de usuario.
 * 
 * @author Xavier Abelló
 */
public class BasicCrawlerController extends Application {

	/** Logger de la classe */
	private static final Logger logger = LoggerFactory.getLogger(BasicCrawlerController.class);

	/**
	 * Metodo main. Llamarlo para iniciar la interfaz de usuario
	 * 
	 * @param args Sin argumentos
	 * @throws Exception Excepcion genérica
	 */
	public static void main(String[] args) throws Exception {
		// Iniciar interfaz JavaFX
		launch(args);
	}


	@Override
	public void start(final Stage pStage) throws Exception {

		try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(BasicCrawlerController.class.getClassLoader().getResource("xabello/webcrawler/javafx/MainLayout.fxml"));
            BorderPane rootLayout = (BorderPane) loader.load();
            MainLayoutController currentController = (MainLayoutController)loader.getController();
            currentController.setJfxStage(pStage);
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            
            pStage.setScene(scene);
            pStage.setTitle("Westwing Crawler");
            
            /*
             * Evento de cerrar ventana
             * Controlar si hay un escaneo en proceso y preguntar al usuario si desea salir
             */
            pStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				
				@Override
				public void handle(WindowEvent event) {
					// Comprovar si hay un proceso en ejecucion y pararlo
					final MainLayoutController currenController = MainLayoutController.getSingletonController();
					if( currenController != null
							&& MainLayoutController.ESTADOESCANEO_PROCESANDO.equals(currenController.getEstadoEscaneo()) ) {
						
						final Stage dialogConfirm = new Stage();
						dialogConfirm.initModality(Modality.APPLICATION_MODAL);
						dialogConfirm.initOwner(pStage);
		                VBox dialogVbox = new VBox(20);
		                dialogVbox.setPadding(new Insets(30, 30, 30, 30));
		                Text dialogText = new Text("Hay un proceso de escaneo en ejecución. Si sale de la aplicación se va a cancelar. Está seguro que desea salir?");
		                dialogText.setWrappingWidth(250);
		                dialogText.setTextAlignment(TextAlignment.JUSTIFY);
		                dialogVbox.getChildren().add(dialogText);
		                Scene dialogScene = new Scene(dialogVbox, 300, 200);
		                dialogConfirm.setScene(dialogScene);
		                dialogConfirm.setTitle("Salir");
		                dialogConfirm.show();
		                
		                // Acciones de los botones
		                
		                // Boton salir
		                Button btnSalir = new Button();
		                btnSalir.setText("Salir");
		                btnSalir.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event1) {
								if( currenController != null
										&& MainLayoutController.ESTADOESCANEO_PROCESANDO.equals(currenController.getEstadoEscaneo()) ) {
									try {
										currenController.getCrawlController().shutdown();
									} catch(Exception e) {
										// Se hace un exit para asegurar que no se quedan hilos activos.
										// Hay ciertas situaciones en que se puede dar el caso si se han producido errores de conexión.
										System.exit(0);
									}
								}
								pStage.close();
							}
						});
		                
		                // Boton volver
		                Button btnVolver = new Button();
		                btnVolver.setText("Volver");
		                btnVolver.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event1) {
								dialogConfirm.close();
							}
						});
		                
		                HBox buttonsBox = new HBox();
		                buttonsBox.setAlignment(Pos.CENTER);
		                buttonsBox.setSpacing(20);
		                buttonsBox.getChildren().add(btnSalir);
		                buttonsBox.getChildren().add(btnVolver);
		                dialogVbox.getChildren().add(buttonsBox);
		                
		                event.consume();	
					}
					else {
						// Se hace un exit para asegurar que no se quedan hilos activos.
						// Hay ciertas situaciones en que se puede dar el caso si se han producido errores de conexión.
						System.exit(0);
					}
				}
			});
            
            pStage.show();
        } catch (IOException e) {
            logger.error("Error", e);
        }
		
	}
}
