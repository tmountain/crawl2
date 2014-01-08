# crawl2

Crawl a list of seed URLs and score sites based on positive and negative phrases.

## Usage

Setup your critieria in resources/ and then do lein run.

Crawl table schema is as follows:

```
CREATE TABLE `crawl` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`,`url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `domain` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `processed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `domainScores` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `score` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `keyword` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) DEFAULT NULL,
  `weight` int(11) DEFAULT '1',
  `bias` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyword` (`keyword`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `matches` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `term` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

Load data as follows:

```
LOAD DATA LOCAL INFILE
'/tmp/keywords.txt'
INTO TABLE
  keyword
FIELDS TERMINATED BY ','
LINES TERMINATED BY '\n'
(keyword, weight, bias);
```

Report data as follows:
```
SELECT
  domain, term
FROM
  matches
INTO OUTFILE
  '/tmp/rawMatch.csv'
FIELDS TERMINATED BY
  ','
LINES TERMINATED BY '\n';

SELECT
  keyword, weight
FROM
  keyword 
INTO OUTFILE
  '/tmp/keywordScore.csv'
FIELDS TERMINATED BY
  ','
LINES TERMINATED BY
  '\n';

$ bin/term_mapper < /tmp/rawMatch.csv > /tmp/domainMatches.csv
$ bin/domain_score < /tmp/domainMatches > /tmp/domainScores.csv

LOAD DATA LOCAL INFILE
  '/tmp/domain_scores.csv'
INTO TABLE
  domainScores
FIELDS TERMINATED BY
  ',' 
LINES TERMINATED BY
  '\n' (domain, score);

SELECT
  domain, score
FROM
  domainScores
WHERE
  score > 100
INTO OUTFILE
  '/tmp/qualifiedDomains'
FIELDS TERMINATED BY
  ','
LINES TERMINATED BY
  '\n';
```

Report data as follows (deprecated):

```
CREATE TEMPORARY TABLE IF NOT EXISTS tmpScores
(SELECT
  d.domain,
  term,
  type,
  qualified,
  weight,
  (IF (count(*) < 10, COUNT(*), 10)) cnt,
  ((IF (COUNT(*) < 10, COUNT(*), 10)) * weight) AS score
FROM matches m
JOIN keyword k ON m.term = k.keyword
JOIN domain d ON m.domain = d.domain
GROUP BY domain, term);

SELECT *
FROM tmpScores
INTO OUTFILE '/tmp/domain_report.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"';

SELECT
  domain,
  term,
  type,
  weight,
  (IF (count(*) < 10, COUNT(*), 10)) cnt,
  ((IF (COUNT(*) < 10, COUNT(*), 10)) * weight) AS score
FROM matches m
JOIN keyword k ON m.term = k.keyword
GROUP BY domain, term
INTO OUTFILE '/tmp/domain_report.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"';
```

Reinitialize as follows:

```
UPDATE domain set processed = 0;
DELETE FROM matches;
DELETE FROM crawl;
```

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
