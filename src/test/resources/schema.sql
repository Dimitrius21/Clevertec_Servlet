DROP TABLE IF EXISTS products;
CREATE TABLE products (
id serial,
name VARCHAR(100),
price INTEGER,
discountType INTEGER);

DROP TABLE IF EXISTS cards;
CREATE TABLE cards (
number bigserial,
discount INTEGER);