-- create first table Picture

CREATE TABLE IF NOT EXISTS Picture (
  idxPicture INT(11) NOT NULL AUTO_INCREMENT,
  picFilename VARCHAR(64) NOT NULL,
  picShotAt DATE DEFAULT NULL,
  picLocation VARCHAR(64) DEFAULT NULL,
  picRemarks VARCHAR(512) DEFAULT NULL,
  PRIMARY KEY (idxPicture)
);

