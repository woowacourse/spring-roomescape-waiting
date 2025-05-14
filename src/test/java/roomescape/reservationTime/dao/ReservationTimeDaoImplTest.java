package roomescape.reservationTime.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.reservation.dao.ReservationDaoImpl;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.dao.ThemeDaoImpl;

@JdbcTest(properties = "spring.sql.init.mode=never")
@Import({ReservationTimeDaoImpl.class, ReservationDaoImpl.class, ThemeDaoImpl.class})
class ReservationTimeDaoImplTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ReservationTimeDaoImpl reservationTimeDaoImpl;
    @Autowired
    private ReservationDaoImpl reservationDaoImpl;
    @Autowired
    private ThemeDaoImpl themeDaoImpl;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();

        jdbcTemplate.execute("DROP TABLE IF EXISTS reservation");
        jdbcTemplate.execute("DROP TABLE IF EXISTS reservation_time");
        jdbcTemplate.execute("DROP TABLE IF EXISTS theme");

        jdbcTemplate.execute("""
                    CREATE TABLE reservation_time (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        start_at VARCHAR(255) NOT NULL,
                        PRIMARY KEY (id)
                    );
                """);

        jdbcTemplate.execute("""
                    CREATE TABLE theme (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        description VARCHAR(255) NOT NULL,
                        thumbnail VARCHAR(255) NOT NULL,
                        PRIMARY KEY (id)
                    );
                """);

        jdbcTemplate.execute("""
                    CREATE TABLE reservation (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        date DATE NOT NULL,
                        time_id BIGINT,
                        theme_id BIGINT,
                        PRIMARY KEY (id),
                        FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                        FOREIGN KEY (theme_id) REFERENCES theme (id)
                    );
                """);

        String insertSqlReservationTime = "INSERT INTO reservation_time(start_at) VALUES (:start_at)";
        namedParameterJdbcTemplate.update(
                insertSqlReservationTime,
                Map.of(
                        "start_at", "10:00"
                )
        );
        namedParameterJdbcTemplate.update(
                insertSqlReservationTime,
                Map.of(
                        "start_at", "11:00"
                )
        );

        String insertSqlTheme = "INSERT INTO theme(name, description, thumbnail) VALUES (:name, :description, :thumbnail)";
        namedParameterJdbcTemplate.update(
                insertSqlTheme,
                Map.of(
                        "name", "방 탈출1",
                        "description", "공포 테마",
                        "thumbnail", "horror.jpg"
                )
        );

        String insertSqlReservation = "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (:name, :date, :time_id, :theme_id)";
        namedParameterJdbcTemplate.update(
                insertSqlReservation,
                Map.of(
                        "name", "홍길동",
                        "date", "2025-05-01",
                        "time_id", 1L,
                        "theme_id", 1L
                )
        );
    }

    @DisplayName("시간 내역을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        assertThat(reservationTimeDaoImpl.findAll()).hasSize(2);
    }

    @DisplayName("시간 내역을 아이디로 조회하는 기능을 구현한다")
    @Test
    void findById() {
        ReservationTime reservationTime = reservationTimeDaoImpl.findById(1L).get();

        assertThat(reservationTime.getId()).isEqualTo(1L);
    }

    @DisplayName("시작 시간으로 시간 내역이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByStartAt() {
        assertThat(reservationTimeDaoImpl.existsByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @DisplayName("해당 시간 아이디로 예약 내역이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByReservationTimeId() {
        assertThat(reservationTimeDaoImpl.existsByReservationTimeId(1L)).isTrue();
    }

    @DisplayName("시간 내역을 추가하는 기능을 구현한다")
    @Test
    void add() {
        ReservationTime reservationTime = new ReservationTime(3L, LocalTime.of(12, 0));

        reservationTimeDaoImpl.add(reservationTime);

        assertThat(reservationTimeDaoImpl.findAll()).hasSize(3);
    }

    @DisplayName("시간 내역을 삭제하는 기능을 구현한다")
    @Test
    void deleteById() {
        reservationTimeDaoImpl.deleteById(2L);

        assertThat(reservationTimeDaoImpl.findAll()).hasSize(1);
    }
}
