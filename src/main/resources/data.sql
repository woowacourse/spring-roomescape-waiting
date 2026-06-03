INSERT INTO theme (id, name, description, thumbnail)
VALUES (1, 'Theme 1', 'Popular theme rank 1', 'https://example.com/theme-1.png'),
       (2, 'Theme 2', 'Popular theme rank 2', 'https://example.com/theme-2.png'),
       (3, 'Theme 3', 'Popular theme rank 3', 'https://example.com/theme-3.png'),
       (4, 'Theme 4', 'Popular theme rank 4', 'https://example.com/theme-4.png'),
       (5, 'Theme 5', 'Popular theme rank 5', 'https://example.com/theme-5.png'),
       (6, 'Theme 6', 'Popular theme rank 6', 'https://example.com/theme-6.png'),
       (7, 'Theme 7', 'Popular theme rank 7', 'https://example.com/theme-7.png'),
       (8, 'Theme 8', 'Popular theme rank 8', 'https://example.com/theme-8.png'),
       (9, 'Theme 9', 'Popular theme rank 9', 'https://example.com/theme-9.png'),
       (10, 'Theme 10', 'Popular theme rank 10', 'https://example.com/theme-10.png'),
       (11, 'Theme 11', 'Out of range reservations only', 'https://example.com/theme-11.png'),
       (12, 'Theme 12', 'No reservations', 'https://example.com/theme-12.png');

INSERT INTO reservation_time (id, start_at)
VALUES (1, '10:00:00'),
       (2, '12:00:00'),
       (3, '14:00:00'),
       (4, '16:00:00'),
       (5, '18:00:00'),
       (6, '20:00:00');

INSERT INTO reservation (
    id,
    guest_name,
    date,
    time_id,
    theme_id,
    status,
    confirmed_token,
    waiting_token
)
VALUES
-- Theme 1
(1, 'guest-1', '2026-05-13', 1, 1, 'CONFIRMED', 1, NULL),
(2, 'guest-2', '2026-05-13', 2, 1, 'CONFIRMED', 1, NULL),
(3, 'guest-3', '2026-05-14', 1, 1, 'CONFIRMED', 1, NULL),
(4, 'guest-4', '2026-05-15', 1, 1, 'CONFIRMED', 1, NULL),
(5, 'guest-5', '2026-05-16', 1, 1, 'CONFIRMED', 1, NULL),
(6, 'guest-6', '2026-05-17', 1, 1, 'CONFIRMED', 1, NULL),
(7, 'guest-7', '2026-05-04', 1, 1, 'CONFIRMED', 1, NULL),
(8, 'guest-8', '2026-05-05', 1, 1, 'WAITING', NULL, 1),
(9, 'guest-9', '2026-05-05', 2, 1, 'WAITING', NULL, 1),
(10, 'guest-10', '2026-05-05', 3, 1, 'CANCELED', NULL, NULL),

-- Theme 2
(11, 'guest-11', '2026-04-29', 1, 2, 'CONFIRMED', 1, NULL),
(12, 'guest-12', '2026-04-29', 2, 2, 'CONFIRMED', 1, NULL),
(13, 'guest-13', '2026-04-30', 1, 2, 'WAITING', NULL, 1),
(14, 'guest-14', '2026-05-01', 1, 2, 'CONFIRMED', 1, NULL),
(15, 'guest-15', '2026-05-02', 1, 2, 'CANCELED', NULL, NULL),
(16, 'guest-16', '2026-05-03', 1, 2, 'WAITING', NULL, 1),
(17, 'guest-17', '2026-05-04', 1, 2, 'CONFIRMED', 1, NULL),
(18, 'guest-18', '2026-05-05', 1, 2, 'WAITING', NULL, 1),
(19, 'guest-19', '2026-05-05', 2, 2, 'CONFIRMED', 1, NULL),

-- Theme 3
(20, 'guest-20', '2026-04-29', 1, 3, 'CONFIRMED', 1, NULL),
(21, 'guest-21', '2026-04-29', 2, 3, 'WAITING', NULL, 1),
(22, 'guest-22', '2026-04-30', 1, 3, 'CONFIRMED', 1, NULL),
(23, 'guest-23', '2026-05-01', 1, 3, 'CONFIRMED', 1, NULL),
(24, 'guest-24', '2026-05-02', 1, 3, 'WAITING', NULL, 1),
(25, 'guest-25', '2026-05-03', 1, 3, 'CANCELED', NULL, NULL),
(26, 'guest-26', '2026-05-04', 1, 3, 'CONFIRMED', 1, NULL),
(27, 'guest-27', '2026-05-05', 1, 3, 'WAITING', NULL, 1),

-- Theme 4
(28, 'guest-28', '2026-04-29', 1, 4, 'CONFIRMED', 1, NULL),
(29, 'guest-29', '2026-04-29', 2, 4, 'WAITING', NULL, 1),
(30, 'guest-30', '2027-04-30', 1, 4, 'CONFIRMED', 1, NULL),
(31, 'guest-31', '2027-05-01', 1, 4, 'WAITING', NULL, 1),
(32, 'guest-32', '2027-05-02', 1, 4, 'CONFIRMED', 1, NULL),
(33, 'guest-33', '2027-05-03', 1, 4, 'CANCELED', NULL, NULL),
(34, 'guest-34', '2027-05-04', 1, 4, 'WAITING', NULL, 1),

-- Theme 5
(35, 'guest-35', '2027-04-29', 1, 5, 'CONFIRMED', 1, NULL),
(36, 'guest-36', '2027-04-29', 2, 5, 'WAITING', NULL, 1),
(37, 'guest-37', '2027-04-30', 1, 5, 'CONFIRMED', 1, NULL),
(38, 'guest-38', '2027-05-01', 1, 5, 'WAITING', NULL, 1),
(39, 'guest-39', '2027-05-02', 1, 5, 'CONFIRMED', 1, NULL),
(40, 'guest-40', '2027-05-03', 1, 5, 'CANCELED', NULL, NULL),

-- Theme 6
(41, 'guest-41', '2027-04-29', 1, 6, 'CONFIRMED', 1, NULL),
(42, 'guest-42', '2027-04-29', 2, 6, 'WAITING', NULL, 1),
(43, 'guest-43', '2027-04-30', 1, 6, 'CONFIRMED', 1, NULL),
(44, 'guest-44', '2027-05-01', 1, 6, 'WAITING', NULL, 1),
(45, 'guest-45', '2027-05-02', 1, 6, 'CONFIRMED', 1, NULL),

-- Theme 7
(46, 'guest-46', '2027-04-29', 1, 7, 'CONFIRMED', 1, NULL),
(47, 'guest-47', '2027-04-29', 2, 7, 'WAITING', NULL, 1),
(48, 'guest-48', '2027-04-30', 1, 7, 'CONFIRMED', 1, NULL),
(49, 'guest-49', '2027-05-01', 1, 7, 'CANCELED', NULL, NULL),

-- Theme 8
(50, 'guest-50', '2027-04-29', 1, 8, 'CONFIRMED', 1, NULL),
(51, 'guest-51', '2027-04-29', 2, 8, 'WAITING', NULL, 1),
(52, 'guest-52', '2027-04-30', 1, 8, 'CONFIRMED', 1, NULL),

-- Theme 9
(53, 'guest-53', '2027-04-29', 1, 9, 'CONFIRMED', 1, NULL),
(54, 'guest-54', '2027-04-29', 2, 9, 'WAITING', NULL, 1),

-- Theme 10
(55, 'guest-55', '2027-04-29', 1, 10, 'CONFIRMED', 1, NULL),

-- Theme 11
(56, 'guest-56', '2027-05-06', 1, 11, 'CONFIRMED', 1, NULL),
(57, 'guest-57', '2027-05-06', 2, 11, 'WAITING', NULL, 1),
(58, 'guest-58', '2027-04-28', 1, 11, 'CANCELED', NULL, NULL);

ALTER TABLE theme
    ALTER COLUMN id RESTART WITH 13;
ALTER TABLE reservation_time
    ALTER COLUMN id RESTART WITH 7;
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 59;
