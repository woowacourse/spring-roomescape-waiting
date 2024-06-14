set referential_integrity false;
truncate table reservation restart identity;
truncate table member restart identity;
truncate table theme restart identity;
truncate table reservation_time restart identity;
truncate table waiting restart identity;
set referential_integrity true;

INSERT INTO member (name, email, password, role) VALUES ('아토', 'atto@gmail.com', 'atto123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('에버', 'treeboss@gmail.com', 'treeboss123!', 'USER');
INSERT INTO member (name, email, password, role) VALUES ('우테코', 'wtc@gmail.com', 'wtc123!', 'ADMIN');

INSERT INTO theme (name, description, thumbnail) VALUES ('n1', 'd1', 't1');
INSERT INTO theme (name, description, thumbnail) VALUES ('n2', 'd2', 't2');
INSERT INTO theme (name, description, thumbnail) VALUES ('n3', 'd3', 't3');
