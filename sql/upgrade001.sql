-- add table Taxon
-- to open mysql client:
-- mysql -u root -p herbier

CREATE TABLE IF NOT EXISTS Taxon (
  idxTaxon INT(11) NOT NULL AUTO_INCREMENT,
  taxName   VARCHAR(64) NOT NULL,
  taxNameFr VARCHAR(64) DEFAULT NULL,
  taxRank   VARCHAR(10) NOT NULL,
  taxParent INT(11) DEFAULT NULL,
  PRIMARY KEY (idxTaxon),
  INDEX index_taxon_parent (taxParent),
  FOREIGN KEY (taxParent) REFERENCES Taxon(idxTaxon)
);

