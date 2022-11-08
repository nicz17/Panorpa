-- add expTrack to Expedition table

-- to update DB:
-- mysql -u nicz -p herbier < upgrade011.sql

ALTER TABLE Expedition
	ADD expTrack VARCHAR(64) DEFAULT NULL;
