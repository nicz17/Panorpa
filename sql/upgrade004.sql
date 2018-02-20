-- add AppParam table
-- add picUpdatedAt

-- to open mysql client:
-- mysql -u root -p herbier


CREATE TABLE IF NOT EXISTS AppParam (
  idxAppParam INT(11) NOT NULL AUTO_INCREMENT,
  apName VARCHAR(64) NOT NULL,
  apDesc VARCHAR(64) DEFAULT NULL,
  apKind VARCHAR(10) NOT NULL,
  apStrVal VARCHAR(512) DEFAULT NULL,
  apDateVal DATETIME DEFAULT NULL,
  apNumVal FLOAT DEFAULT NULL,
  PRIMARY KEY (idxAppParam)
);

INSERT INTO AppParam VALUES (
       NULL, 'taxonTreeSel', 'Taxon tree selection', 'INT',
       NULL, NULL, NULL);

INSERT INTO AppParam VALUES (
       NULL, 'websiteUpload', 'Last website upload', 'DATE',
       NULL, NULL, NULL);

INSERT INTO AppParam VALUES (
       NULL, 'backupBook', 'Last MyBook backup', 'DATE',
       NULL, NULL, NULL);

ALTER TABLE Picture
	ADD picUpdatedAt DATETIME DEFAULT NULL;

