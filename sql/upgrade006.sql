-- add Location FK to Picture

-- to open mysql client:
-- mysql -u root -p herbier

ALTER TABLE Picture 
	ADD picIdxLocation INT(11) DEFAULT NULL,
	ADD FOREIGN KEY (picIdxLocation) REFERENCES Location(idxLocation);

