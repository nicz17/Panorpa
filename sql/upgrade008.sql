-- add Expedition table
-- update picShotAt to datetime

-- to open mysql client:
-- mysql -u root -p herbier


CREATE TABLE IF NOT EXISTS Expedition (
  idxExpedition INT(11) NOT NULL AUTO_INCREMENT,
  expName VARCHAR(64) NOT NULL,
  expDesc VARCHAR(1024) DEFAULT NULL,
  expLocation INT(11) NOT NULL,
  expFrom DATETIME NOT NULL,
  expTo   DATETIME NOT NULL,
  PRIMARY KEY (idxExpedition),
  INDEX index_expedition_location (expLocation),
  FOREIGN KEY (expLocation) REFERENCES Location(idxLocation)
);

ALTER TABLE Picture
	MODIFY picShotAt DATETIME;
	
	
