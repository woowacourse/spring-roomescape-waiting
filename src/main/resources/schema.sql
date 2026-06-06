CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);

CREATE TABLE theme
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(20)  NOT NULL,
    description  VARCHAR(255) NOT NULL,
    image_url    VARCHAR(255) NOT NULL,
    running_time BIGINT       NOT NULL,
    PRIMARY KEY (id)
);


CREATE TABLE slot
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    date     DATE   NOT NULL,
    time_id  BIGINT NOT NULL,
    theme_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    UNIQUE (date, time_id, theme_id)
);

CREATE TABLE reservation
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    slot_id           BIGINT      NOT NULL,
    name              VARCHAR(20) NOT NULL,
    status            VARCHAR(20) NOT NULL,
    created_at        TIMESTAMP   NOT NULL,
    confirmed_slot_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'CONFIRMED' THEN slot_id END),
    PRIMARY KEY (id),
    FOREIGN KEY (slot_id) REFERENCES slot (id),
    CONSTRAINT uq_applicant UNIQUE (slot_id, name),
    CONSTRAINT uq_confirmed UNIQUE (confirmed_slot_id)
);
