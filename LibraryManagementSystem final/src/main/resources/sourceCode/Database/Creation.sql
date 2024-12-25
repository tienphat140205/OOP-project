CREATE DATABASE IF NOT EXISTS LIBRARY;
USE LIBRARY;
CREATE TABLE IF NOT EXISTS BOOK
(
    ISBN            VARCHAR(30) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    genre           VARCHAR(100),
    publisher       VARCHAR(255),
    publicationDate VARCHAR(20),
    language        VARCHAR(50),
    pageNumber      INT,
    imageUrl        VARCHAR(255),
    description     TEXT,
    quantity        INT
);
CREATE TABLE IF NOT EXISTS USER
(
    userId         VARCHAR(10) PRIMARY KEY,
    name           VARCHAR(50),
    identityNumber VARCHAR(20),
    birth          DATE,
    gender         VARCHAR(6),
    phoneNumber    VARCHAR(20),
    email          VARCHAR(50),
    address        VARCHAR(255),
    password       VARCHAR(50),
    role           VARCHAR(10)
);
CREATE TABLE IF NOT EXISTS TICKET
(
    ticketId     INT AUTO_INCREMENT PRIMARY KEY,
    userId       VARCHAR(10),
    ISBN         VARCHAR(30),
    borrowedDate DATE,
    returnedDate DATE,
    quantity     INT,
    FOREIGN KEY (userId) REFERENCES USER (userId) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES BOOK (ISBN) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS FEEDBACK
(
    feedbackId INT AUTO_INCREMENT PRIMARY KEY,
    userId     VARCHAR(10),
    ISBN       VARCHAR(30),
    comment    TEXT,
    rating     INT,
    date       DATE,
    FOREIGN KEY (userId) REFERENCES USER (userId) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ISBN) REFERENCES BOOK (ISBN) ON UPDATE CASCADE ON DELETE CASCADE
);
