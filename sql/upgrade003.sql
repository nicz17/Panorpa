-- add Taxon order and typical flag

-- to open mysql client:
-- mysql -u root -p herbier


ALTER TABLE Taxon
	ADD taxOrder   INT(11)    DEFAULT 0,
	ADD taxTypical TINYINT(1) DEFAULT 0;

