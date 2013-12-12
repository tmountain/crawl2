# crawl2

Crawl a list of seed URLs and score sites based on positive and negative phrases.

## Usage

Setup your critieria in resources/ and then do lein run.

Crawl table schema is as follows:

CREATE TABLE `crawl` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`,`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
