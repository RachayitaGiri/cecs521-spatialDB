/* Create and use the database 'PublicSafety' */
CREATE DATABASE PublicSafety;
USE PublicSafety;

/* Creating Tthe necessary tables*/
CREATE TABLE zone (
	zone_id int unsigned not null,
	zone_name varchar(70),
	squad_id int unsigned,
	area_vertex_count int unsigned,
	zone_area polygon not null SRID 0, -- SRID 4326 not supported by MYSQL OpenGIS
	PRIMARY KEY(zone_id),
	SPATIAL INDEX(zone_area)
) ENGINE = MYISAM;

CREATE TABLE officer (
	badge_id int unsigned not null,
	officer_name varchar(50),
	squad_id int unsigned,
	location point not null SRID 0,
	PRIMARY KEY(badge_id),
	SPATIAL INDEX(location)
) ENGINE = MYISAM;

CREATE TABLE route (
	route_id int unsigned not null,
	route_vertex_count int unsigned, 
	route linestring not null SRID 0,
	PRIMARY KEY(route_id),
	SPATIAL INDEX(route)
) ENGINE = MYISAM;

CREATE TABLE incident (
	incident_id int unsigned not null,
	incident_type varchar(50),
	incident_location point not null SRID 0,
	PRIMARY KEY(incident_id),
	SPATIAL INDEX(incident_location)
) ENGINE = MYISAM;

