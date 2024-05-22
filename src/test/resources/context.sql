DELETE FROM member WHERE email = 'admin@email.com';
INSERT INTO member (email, name, password, role) VALUES ('admin@email.com', '어드민', 'adminpassword', 'ADMIN');
