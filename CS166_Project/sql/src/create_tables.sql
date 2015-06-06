DROP TABLE WORK_EXPR;
DROP TABLE EDUCATIONAL_DETAILS;
DROP TABLE MESSAGE;
DROP TABLE CONNECTION_USR;
DROP TABLE USR;


CREATE TABLE USR(
	userId varchar(255) UNIQUE NOT NULL, 
	password varchar(20) NOT NULL,
	email text NOT NULL,
	name varchar(50),
	dateOfBirth date,
	Primary Key(userId));

CREATE TABLE WORK_EXPR(
	userId varchar(255) NOT NULL, 
	company varchar(50) NOT NULL, 
	role varchar(50) NOT NULL,
	location varchar(50),
	startDate date,
	endDate date,
	PRIMARY KEY(userId,company,role,startDate),
	FOREIGN KEY(userId) REFERENCES USR(userId) );

CREATE TABLE EDUCATIONAL_DETAILS(
	userId varchar(255) NOT NULL, 
	instituitionName varchar(50) NOT NULL, 
	major varchar(50) NOT NULL,
	degree varchar(50) NOT NULL,
	startdate date,
	enddate date,
	PRIMARY KEY(userId,major,degree),
	FOREIGN KEY(userId) REFERENCES USR(userId));

CREATE TABLE MESSAGE(
	msgId integer UNIQUE NOT NULL, 
	senderId varchar(255) NOT NULL,
	receiverId varchar(255) NOT NULL,
	contents varchar(500) NOT NULL,
	sendTime timestamp, 
	deleteStatus integer,
	status varchar(30) NOT NULL,
	PRIMARY KEY(msgId),
    FOREIGN KEY(senderId) REFERENCES USR(userId),
    FOREIGN KEY(receiverId) REFERENCES USR(userId));

CREATE TABLE CONNECTION_USR(
	userId varchar(255) NOT NULL, 
	connectionId varchar(255) NOT NULL, 
	status varchar(30) NOT NULL,
	PRIMARY KEY(userId,connectionId),
	FOREIGN KEY(userId) REFERENCES USR(userId),
    FOREIGN KEY(connectionId) REFERENCES USR(userId));


