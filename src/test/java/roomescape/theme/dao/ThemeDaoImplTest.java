package roomescape.theme.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
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
import roomescape.reservationTime.dao.ReservationTimeDaoImpl;
import roomescape.theme.domain.Theme;

@JdbcTest(properties = "spring.sql.init.mode=never")
@Import({ThemeDaoImpl.class, ReservationTimeDaoImpl.class, ReservationDaoImpl.class})
class ThemeDaoImplTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ThemeDaoImpl themeDaoImpl;
    @Autowired
    private ReservationTimeDaoImpl reservationTimeDaoImpl;
    @Autowired
    private ReservationDaoImpl reservationDaoImpl;

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

        String insertSqlTheme = "INSERT INTO theme(name, description, thumbnail) VALUES (:name, :description, :thumbnail)";
        namedParameterJdbcTemplate.update(
                insertSqlTheme,
                Map.of(
                        "name", "방 탈출1",
                        "description", "공포 테마",
                        "thumbnail", "horror.jpg"
                )
        );
        namedParameterJdbcTemplate.update(
                insertSqlTheme,
                Map.of(
                        "name", "방 탈출2",
                        "description", "동화 테마",
                        "thumbnail", "fairytale.jpg"
                )
        );

        String insertSqlReservation = "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (:name, :date, :time_id, :theme_id)";
        namedParameterJdbcTemplate.update(
                insertSqlReservation,
                Map.of(
                        "name", "홍길동",
                        "date", LocalDate.now().minusDays(2).toString(),
                        "time_id", 1L,
                        "theme_id", 1L
                )
        );
    }

    @DisplayName("테마 내역을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        List<Theme> themes = themeDaoImpl.findAll();

        assertThat(themes).hasSize(2);
    }

    @DisplayName("테마 내역을 아이디로 조회하는 기능을 구현한다")
    @Test
    void findById() {
        Theme theme = themeDaoImpl.findById(1L).get();

        assertThat(theme.getId()).isEqualTo(1L);
    }

    @DisplayName("일정 기간 동안 순위권 내의 테마 내역을 조회하는 기능을 구현한다")
    @Test
    void findRankedByPeriod() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);

        List<Theme> rankedThemes = themeDaoImpl.findRankedByPeriod(startDate, endDate, 10);

        assertThat(rankedThemes).hasSize(1);
    }

    @DisplayName("테마 이름으로 테마 내역이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByName() {
        assertThat(themeDaoImpl.existsByName("방 탈출1")).isTrue();
    }

    @DisplayName("해당 테마 아이디로 예약 내역이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByReservationThemeId() {
        assertThat(themeDaoImpl.existsByReservationThemeId(1L)).isTrue();
    }

    @DisplayName("새로운 테마 내역을 추가하는 기능을 구현한다")
    @Test
    void add() {
        Theme theme = new Theme(1L, "name1", "description1", "thumbnail1");

        themeDaoImpl.add(theme);

        assertThat(themeDaoImpl.findAll()).hasSize(3);
    }

    @DisplayName("기존의 테마 내역을 삭제하는 기능을 구현한다")
    @Test
    void deleteById() {
        themeDaoImpl.deleteById(2L);

        assertThat(themeDaoImpl.findAll()).hasSize(1);
    }
}
