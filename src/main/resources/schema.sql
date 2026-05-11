CREATE TABLE theme (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image VARCHAR(255),

    PRIMARY KEY (id)
);

CREATE TABLE reservation_time (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    start_at TIME NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE reservation (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    theme_id BIGINT NOT NULL,
    date    DATE NOT NULL,
    time_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id) ON DELETE CASCADE,
    FOREIGN KEY (theme_id) REFERENCES theme (id) ON DELETE CASCADE
);

INSERT INTO theme (name, description, image) VALUES ('은하수', '은하수 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('지구', '지구 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('수성', '수성 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('금성', '금성 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('화성', '화성 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('목성', '목성 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('토성', '토성 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('보이저', '보이저 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('아폴로', '아폴로 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('허블', '허블 테마방입니다.', 'http.jpg');
INSERT INTO theme (name, description, image) VALUES ('안드로메다', '안드로메다 테마방입니다.', 'http.jpg');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('11:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');
INSERT INTO reservation_time (start_at) VALUES ('14:00');
INSERT INTO reservation_time (start_at) VALUES ('15:00');
INSERT INTO reservation_time (start_at) VALUES ('16:00');
INSERT INTO reservation_time (start_at) VALUES ('17:00');
INSERT INTO reservation_time (start_at) VALUES ('18:00');
INSERT INTO reservation_time (start_at) VALUES ('19:00');
INSERT INTO reservation_time (start_at) VALUES ('20:00');

INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 1L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 1L, '2026-05-05', 4L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('워넬', 2L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('도우너', 2L, '2026-05-05', 2L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 2L, '2026-05-05', 4L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 3L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 3L, '2026-05-05', 4L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 4L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 5L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 6L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 7L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 8L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 9L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 10L, '2026-05-05', 1L);
INSERT INTO reservation (name, theme_id, date, time_id) VALUES ('로치', 11L, '2026-05-05', 1L);
