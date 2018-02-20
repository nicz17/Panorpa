-- add Location table

-- to open mysql client:
-- mysql -u root -p herbier


CREATE TABLE IF NOT EXISTS Location (
  idxLocation INT(11) NOT NULL AUTO_INCREMENT,
  locName VARCHAR(64) NOT NULL,
  locDesc VARCHAR(1024) DEFAULT NULL,
  locKind VARCHAR(64) DEFAULT NULL,
  locTown VARCHAR(64) DEFAULT NULL,
  locRegion VARCHAR(64) DEFAULT NULL,
  locState VARCHAR(64) DEFAULT NULL,
  locLongitude FLOAT DEFAULT NULL,
  locLatitude FLOAT DEFAULT NULL,
  locAltitude INT(11) DEFAULT NULL,
  PRIMARY KEY (idxLocation)
);

ALTER TABLE Location
	ADD UNIQUE(locName);
