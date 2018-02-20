-- make Taxon name unique,
-- add Taxon FK to Picture

-- to open mysql client:
-- mysql -u root -p herbier

ALTER TABLE Taxon
	ADD UNIQUE(taxName);

ALTER TABLE Picture 
	ADD picTaxon INT(11) DEFAULT NULL,
	ADD FOREIGN KEY (picTaxon) REFERENCES Taxon(idxTaxon);

