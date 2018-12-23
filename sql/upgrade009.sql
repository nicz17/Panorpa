-- add locMapZoom to Location

-- to open mysql client:
-- mysql -u root -p herbier

ALTER TABLE Location 
	ADD locMapZoom INT(2) NOT NULL DEFAULT 14;

