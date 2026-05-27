INSERT INTO theme (id, name, description, thumbnail) VALUES
    (1, 'Theme 1', 'Popular theme rank 1', 'https://example.com/theme-1.png'),
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

INSERT INTO reservation_time (id, start_at) VALUES
    (1, '10:00:00'),
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
    last_modified_at
) VALUES
      (1, 'guest-1', '2026-05-13', 1, 1, 'CONFIRMED', '2026-04-29 09:00:00'),
      (2, 'guest-2', '2026-05-13', 2, 1, 'CONFIRMED', '2026-04-29 09:01:00'),
      (3, 'guest-3', '2026-05-14', 1, 1, 'CONFIRMED', '2026-04-29 09:02:00'),
      (4, 'guest-4', '2026-05-15', 1, 1, 'CONFIRMED', '2026-04-29 09:03:00'),
      (5, 'guest-5', '2026-05-16', 1, 1, 'CONFIRMED', '2026-04-29 09:04:00'),
      (6, 'guest-6', '2026-05-17', 1, 1, 'CONFIRMED', '2026-04-29 09:05:00'),
      (7, 'guest-7', '2026-05-04', 1, 1, 'CONFIRMED', '2026-04-29 09:06:00'),
      (8, 'guest-8', '2026-05-05', 1, 1, 'WAITING', '2026-04-29 09:07:00'),
      (9, 'guest-9', '2026-05-05', 2, 1, 'WAITING', '2026-04-29 09:08:00'),
      (10, 'guest-10', '2026-05-05', 3, 1, 'CANCELED', '2026-04-29 09:09:00'),

      (11, 'guest-11', '2026-04-29', 1, 2, 'CONFIRMED', '2026-04-29 09:10:00'),
      (12, 'guest-12', '2026-04-29', 2, 2, 'CONFIRMED', '2026-04-29 09:11:00'),
      (13, 'guest-13', '2026-04-30', 1, 2, 'WAITING', '2026-04-29 09:12:00'),
      (14, 'guest-14', '2026-05-01', 1, 2, 'CONFIRMED', '2026-04-29 09:13:00'),
      (15, 'guest-15', '2026-05-02', 1, 2, 'CANCELED', '2026-04-29 09:14:00'),
      (16, 'guest-16', '2026-05-03', 1, 2, 'WAITING', '2026-04-29 09:15:00'),
      (17, 'guest-17', '2026-05-04', 1, 2, 'CONFIRMED', '2026-04-29 09:16:00'),
      (18, 'guest-18', '2026-05-05', 1, 2, 'WAITING', '2026-04-29 09:17:00'),
      (19, 'guest-19', '2026-05-05', 2, 2, 'CONFIRMED', '2026-04-29 09:18:00'),

      (20, 'guest-20', '2026-04-29', 1, 3, 'CONFIRMED', '2026-04-29 09:19:00'),
      (21, 'guest-21', '2026-04-29', 2, 3, 'WAITING', '2026-04-29 09:20:00'),
      (22, 'guest-22', '2026-04-30', 1, 3, 'CONFIRMED', '2026-04-29 09:21:00'),
      (23, 'guest-23', '2026-05-01', 1, 3, 'CONFIRMED', '2026-04-29 09:22:00'),
      (24, 'guest-24', '2026-05-02', 1, 3, 'WAITING', '2026-04-29 09:23:00'),
      (25, 'guest-25', '2026-05-03', 1, 3, 'CANCELED', '2026-04-29 09:24:00'),
      (26, 'guest-26', '2026-05-04', 1, 3, 'CONFIRMED', '2026-04-29 09:25:00'),
      (27, 'guest-27', '2026-05-05', 1, 3, 'WAITING', '2026-04-29 09:26:00'),

      (28, 'guest-28', '2026-04-29', 1, 4, 'CONFIRMED', '2026-04-29 09:27:00'),
      (29, 'guest-29', '2026-04-29', 2, 4, 'WAITING', '2026-04-29 09:28:00'),
      (30, 'guest-30', '2027-04-30', 1, 4, 'CONFIRMED', '2026-04-29 09:29:00'),
      (31, 'guest-31', '2027-05-01', 1, 4, 'WAITING', '2026-04-29 09:30:00'),
      (32, 'guest-32', '2027-05-02', 1, 4, 'CONFIRMED', '2026-04-29 09:31:00'),
      (33, 'guest-33', '2027-05-03', 1, 4, 'CANCELED', '2026-04-29 09:32:00'),
      (34, 'guest-34', '2027-05-04', 1, 4, 'WAITING', '2026-04-29 09:33:00'),

      (35, 'guest-35', '2027-04-29', 1, 5, 'CONFIRMED', '2026-04-29 09:34:00'),
      (36, 'guest-36', '2027-04-29', 2, 5, 'WAITING', '2026-04-29 09:35:00'),
      (37, 'guest-37', '2027-04-30', 1, 5, 'CONFIRMED', '2026-04-29 09:36:00'),
      (38, 'guest-38', '2027-05-01', 1, 5, 'WAITING', '2026-04-29 09:37:00'),
      (39, 'guest-39', '2027-05-02', 1, 5, 'CONFIRMED', '2026-04-29 09:38:00'),
      (40, 'guest-40', '2027-05-03', 1, 5, 'CANCELED', '2026-04-29 09:39:00'),

      (41, 'guest-41', '2027-04-29', 1, 6, 'CONFIRMED', '2026-04-29 09:40:00'),
      (42, 'guest-42', '2027-04-29', 2, 6, 'WAITING', '2026-04-29 09:41:00'),
      (43, 'guest-43', '2027-04-30', 1, 6, 'CONFIRMED', '2026-04-29 09:42:00'),
      (44, 'guest-44', '2027-05-01', 1, 6, 'WAITING', '2026-04-29 09:43:00'),
      (45, 'guest-45', '2027-05-02', 1, 6, 'CONFIRMED', '2026-04-29 09:44:00'),

      (46, 'guest-46', '2027-04-29', 1, 7, 'CONFIRMED', '2026-04-29 09:45:00'),
      (47, 'guest-47', '2027-04-29', 2, 7, 'WAITING', '2026-04-29 09:46:00'),
      (48, 'guest-48', '2027-04-30', 1, 7, 'CONFIRMED', '2026-04-29 09:47:00'),
      (49, 'guest-49', '2027-05-01', 1, 7, 'CANCELED', '2026-04-29 09:48:00'),

      (50, 'guest-50', '2027-04-29', 1, 8, 'CONFIRMED', '2026-04-29 09:49:00'),
      (51, 'guest-51', '2027-04-29', 2, 8, 'WAITING', '2026-04-29 09:50:00'),
      (52, 'guest-52', '2027-04-30', 1, 8, 'CONFIRMED', '2026-04-29 09:51:00'),

      (53, 'guest-53', '2027-04-29', 1, 9, 'CONFIRMED', '2026-04-29 09:52:00'),
      (54, 'guest-54', '2027-04-29', 2, 9, 'WAITING', '2026-04-29 09:53:00'),

      (55, 'guest-55', '2027-04-29', 1, 10, 'CONFIRMED', '2026-04-29 09:54:00'),

      (56, 'guest-56', '2027-05-06', 1, 11, 'CONFIRMED', '2026-04-29 09:55:00'),
      (57, 'guest-57', '2027-05-06', 2, 11, 'WAITING', '2026-04-29 09:56:00'),
      (58, 'guest-58', '2027-04-28', 1, 11, 'CANCELED', '2026-04-29 09:57:00');

ALTER TABLE theme ALTER COLUMN id RESTART WITH 13;
ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 7;
ALTER TABLE reservation ALTER COLUMN id RESTART WITH 59;
