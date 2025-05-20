-- reservation_time 테이블에 대한 INSERT
INSERT INTO reservation_time(start_at) VALUES ('12:00');
INSERT INTO reservation_time(start_at) VALUES ('13:00');
INSERT INTO reservation_time(start_at) VALUES ('14:00');
INSERT INTO reservation_time(start_at) VALUES ('15:00');
INSERT INTO reservation_time(start_at) VALUES ('16:00');
INSERT INTO reservation_time(start_at) VALUES ('17:00');
INSERT INTO reservation_time(start_at) VALUES ('18:00');
INSERT INTO reservation_time(start_at) VALUES ('19:00');
INSERT INTO reservation_time(start_at) VALUES ('20:00');
INSERT INTO reservation_time(start_at) VALUES ('21:00');
INSERT INTO reservation_time(start_at) VALUES ('22:00');

-- theme 테이블에 대한 INSERT
INSERT INTO theme(name, description, thumbnail) VALUES ('The Haunted Mansion', 'Solve the mysteries of the haunted mansion to escape.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Secret Agent Mission', 'Complete your secret agent mission before time runs out.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Pirate''s Treasure', 'Find the hidden pirate''s treasure in this thrilling adventure.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Alien Invasion', 'Defend Earth from an alien invasion and escape safely.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Mystery of the Pharaoh', 'Uncover the secrets of the ancient pharaoh''s tomb.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Escape from the Asylum', 'Escape the asylum by solving puzzles and clues.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Lost City', 'Discover the secrets of the lost city and find your way out.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Time Travel Trouble', 'Fix the timeline and escape the time travel trouble.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Zombie Apocalypse', 'Survive the zombie apocalypse and find a safe exit.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Enchanted Forest', 'Navigate through the enchanted forest to find your way out.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Bank Heist', 'Plan and execute the perfect bank heist to escape.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Spy School', 'Graduate from spy school by completing challenging missions.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Cursed Jewel', 'Break the curse of the jewel and escape the danger.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('Space Station Escape', 'Escape the malfunctioning space station before it''s too late.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Wizard''s Tower', 'Climb the wizard''s tower and solve magical puzzles.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Secret Laboratory', 'Escape the secret laboratory filled with experiments gone wrong.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Forgotten Dungeon', 'Find your way out of the forgotten dungeon.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Vampire''s Lair', 'Escape the vampire''s lair before dawn.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Arctic Expedition', 'Survive the harsh conditions of the arctic expedition.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');
INSERT INTO theme(name, description, thumbnail) VALUES ('The Wild West', 'Escape the wild west town before the showdown.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg');

-- member 테이블에 대한 INSERT
INSERT INTO member(name, role, email, password) VALUES ('admin', 'ADMIN', 'admin@email.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Alice', 'USER', 'alice@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Bob', 'USER', 'bob@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Charlie', 'USER', 'charlie@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('David', 'USER', 'david@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Eve', 'USER', 'eve@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Frank', 'USER', 'frank@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Grace', 'USER', 'grace@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Heidi', 'USER', 'heidi@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Ivan', 'USER', 'ivan@example.com', 'password');
INSERT INTO member (name, role, email, password) VALUES ('Judy', 'USER', 'judy@example.com', 'password');

-- reservation 테이블에 대한 INSERT
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (3, '2025-05-04', 3, 9, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (7, '2025-05-04', 9, 7, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (2, '2025-05-03', 2, 16, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (9, '2025-05-07', 3, 19, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (5, '2025-05-06', 10, 5, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (1, '2025-05-04', 9, 16, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (6, '2025-05-03', 7, 20, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (4, '2025-05-06', 7, 11, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (8, '2025-05-07', 7, 13, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (10, '2025-05-04', 5, 4, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (2, '2025-05-05', 4, 2, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (5, '2025-05-08', 8, 15, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (9, '2025-05-09', 1, 7, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (7, '2025-05-03', 6, 11, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (1, '2025-05-08', 2, 5, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (3, '2025-05-07', 10, 18, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (6, '2025-05-06', 5, 20, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (8, '2025-05-05', 9, 3, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (4, '2025-05-09', 7, 7, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (10, '2025-05-08', 11, 13, 'RESERVED');
INSERT INTO reservation(member_id, date, time_id, theme_id, status) VALUES (10, '2025-05-30', 1, 1, 'RESERVED');

-- waiting 테이블에 대한 INSERT
INSERT INTO waiting(member_id, date, time_id, theme_id, waiting_order) VALUES (10, '2025-05-30', 1, 1, 1);
