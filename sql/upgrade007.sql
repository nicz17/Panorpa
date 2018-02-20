-- add picRating to Picture

-- to open mysql client:
-- mysql -u root -p herbier

ALTER TABLE Picture 
	ADD picRating INT(2) NOT NULL DEFAULT 3;

