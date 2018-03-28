# Items web crawler

This is a webcrawler that can be configured to scrape items from web pages. 
The application has a UI interface using JavaFX.

This is a standard project. If you need a customized version for special crawling contact me at xabello.dev@gmail.com

## Build and execution

This is a Java project under Maven configuration.
You need to have Java and Maven installed in your system in order to build and execute the project.

Execute the following command to build project

    mvn package

This will generate jar executable files in target folder. 

To execute application use the following command

    java -jar webcrawler-1.1-jar-with-dependencies.jar

## Database installation

Before you can execute a crawling operation a database must be setup. 
The scripts are under configuration folder of the project. 

To configure the database connection modify **config.properties** file in the project resource folder.

    persistence.db.url=jdbc:mysql://localhost:3306/crwldb
    persistence.db.user=crwlusr1
    persistence.db.pass=crwlusr2


## Proxy configuration

The crawler allows to configure a proxy connection tunnel.

To enable this feature activate te **config.properties** file property crawler.use_proxy

    crawler.use_proxy=true

In the database table **proxy_server** add as many proxys as you like

* ip: Proxy server ip
* port: Proxy server port
* username: Username to authenticate proxy if needed (otherwise set null)
* userpwd: Password to authenticate proxy if needed (otherwise set null)
* enabled: If this is set to false, the proxy will be ignored

The crawler will try to use the proxys in the order of it table id. If one fails it will try the following. If all 
proxis fail, an error will be thrown. 

## DOM Mapping elements

The crawler must be configured in order to detect the elements that will be extracted from pages and stored in database.

    crawler.dom_mapping.title=productdetail-headline
    crawler.dom_mapping.description=productdetail-moreinfo__details
    crawler.dom_mapping.price=productdetail-price__discounted
    crawler.dom_mapping.image=imageProduct