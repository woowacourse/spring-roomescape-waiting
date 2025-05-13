-- member 테이블에 대한 INSERT
INSERT INTO member(name, email, password, role) VALUES ('서프', 'admin@email.com', 'password', 'ADMIN');
INSERT INTO member(name, email, password, role) VALUES ('앤지', 'normal@email.com', 'password', 'NORMAL');

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

-- reservation 테이블에 대한 INSERT
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-24', 3, 9, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-24', 9, 7, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-23', 2, 16, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-27', 3, 19, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-26', 10, 5, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-24', 9, 16, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-23', 7, 20, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-26', 7, 11, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-27', 7, 13, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-24', 5, 4, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-25', 4, 2, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-28', 8, 15, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-29', 1, 7, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-23', 6, 11, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-28', 2, 5, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-27', 10, 18, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-26', 5, 20, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-25', 9, 3, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-29', 7, 7, 1);
INSERT INTO reservation(date, time_id, theme_id, member_id) VALUES ('2025-04-28', 11, 13, 1);
