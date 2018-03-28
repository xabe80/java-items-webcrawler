/* *
 * CREATE TABLE  scraped_data
 */
CREATE TABLE `scraped_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `codigo_item` varchar(30) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `descripcion` varchar(2000) DEFAULT NULL,
  `imagen` varchar(100) DEFAULT NULL,
  `precio` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo_item_UNIQUE` (`codigo_item`)
) ENGINE=InnoDB AUTO_INCREMENT=6535 DEFAULT CHARSET=latin1;

/* *
 * CREATE TABLE  proxy_server
 */
CREATE TABLE `proxy_server` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(20) NOT NULL,
  `port` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `userpwd` varchar(50) DEFAULT NULL,
  `enabled` varchar(45) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
