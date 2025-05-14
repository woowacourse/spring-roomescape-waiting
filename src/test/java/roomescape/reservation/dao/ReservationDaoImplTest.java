package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.dao.ReservationTimeDaoImpl;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.dao.ThemeDaoImpl;
import roomescape.theme.domain.Theme;

@JdbcTest(properties = "spring.sql.init.mode=never")
@Import({ReservationDaoImpl.class, ReservationTimeDaoImpl.class, ThemeDaoImpl.class})
class ReservationDaoImplTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private ReservationDaoImpl reservationDaoImpl;
    @Autowired
    private ReservationTimeDaoImpl reservationTimeDaoImpl;
    @Autowired
    private ThemeDaoImpl themeDaoImpl;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();

        jdbcTemplate.execute("DROP TABLE IF EXISTS reservation");
        jdbcTemplate.execute("DROP TABLE IF EXISTS reservation_time");
        jdbcTemplate.execute("DROP TABLE IF EXISTS theme");
        jdbcTemplate.execute("DROP TABLE IF EXISTS member");

        jdbcTemplate.execute("""
                    CREATE TABLE member (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(255) NOT NULL, 
                        PRIMARY KEY (id)
                    );
                """);

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
                        member_id BIGINT, 
                        PRIMARY KEY (id),
                        FOREIGN KEY (time_id) REFERENCES reservation_time (id),
                        FOREIGN KEY (theme_id) REFERENCES theme (id),
                        FOREIGN KEY (member_id) REFERENCES member (id)
                    );
                """);

        String insertSqlMember = "INSERT INTO member(name, email, password, role) VALUES (:name, :email, :password, :role)";
        namedParameterJdbcTemplate.update(
                insertSqlMember,
                Map.of(
                        "name", "홍길동",
                        "email", "hong@email.com",
                        "password", "password123",
                        "role", "user"
                )
        );

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

        String insertSqlReservation = "INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (:name, :date, :time_id, :theme_id, :member_id)";
        namedParameterJdbcTemplate.update(
                insertSqlReservation,
                Map.of(
                        "name", "홍길동",
                        "date", "2025-05-01",
                        "time_id", 1L,
                        "theme_id", 1L,
                        "member_id", 1L
                )
        );
    }

    @DisplayName("예약 내역을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        List<Reservation> reservations = reservationDaoImpl.findAll();

        assertThat(reservations).hasSize(1);
    }

    @DisplayName("예약 내역을 아이디로 조회하는 기능을 구현한다")
    @Test
    void findById() {
        Reservation reservation = reservationDaoImpl.findById(1L).get();

        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @DisplayName("회원 아이디로 예약 내역을 조회하는 기능을 구현한다")
    @Test
    void findAllByMemberId() {
        List<Reservation> reservations = reservationDaoImpl.findAllByMemberId(1L);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(reservations).hasSize(1);
        softly.assertThat(reservations).allMatch(reservation -> reservation.getMember().getId().equals(1L));
        softly.assertAll();
    }

    @DisplayName("회원 아이디, 테마 아이디, 날짜 범위로 예약 내역을 조회하는 기능을 구현한다")
    @Test
    void findAllByMemberAndThemeAndDate() {
        List<Reservation> reservations = reservationDaoImpl.findAllByMemberAndThemeAndDate(
                1L,
                1L,
                LocalDate.parse("2025-05-01"),
                LocalDate.parse("2025-05-03")
        );

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(reservations).hasSize(1);
        softly.assertThat(reservations).allMatch(reserve ->
                reserve.getMember().getId().equals(1L) &&
                        reserve.getTheme().getId().equals(1L) &&
                        !reserve.getDate().isBefore(LocalDate.parse("2025-05-01")) &&
                        !reserve.getDate().isAfter(LocalDate.parse("2025-05-03"))
        );
        softly.assertAll();
    }

    @DisplayName("회원 아이디, 테마 아이디, 날짜 범위로 예약 내역을 조회할 때 조건에 맞는 예약이 없으면 빈 리스트를 반환한다")
    @Test
    void findAllByMemberAndThemeAndDate_WhenNoMatchingReservations() {
        List<Reservation> reservations = reservationDaoImpl.findAllByMemberAndThemeAndDate(
                1L,
                1L,
                LocalDate.parse("2025-06-01"),
                LocalDate.parse("2025-06-03")
        );

        assertThat(reservations).isEmpty();
    }

    @DisplayName("날짜와 시간 아이디로 예약 내역이 존재하는지 확인하는 기능을 구현한다")
    @Test
    void existsByDateAndTimeId() {
        assertThat(reservationDaoImpl.existsByDateAndTimeId(LocalDate.of(2025, 5, 1), 1L)).isTrue();
    }

    @DisplayName("예약 내역을 추가하는 기능을 구현한다")
    @Test
    void add() {
        Member member = new Member(1L, "홍길동", "hong@email.com", "password123", Role.USER);
        ReservationTime time = new ReservationTime(2L, LocalTime.parse("11:00"));
        Theme theme = new Theme(1L, "방 탈출1", "공포 테마", "horror.jpg");
        Reservation reservation = new Reservation(
                member,
                LocalDate.now().plusDays(1),
                time,
                theme
        );

        reservationDaoImpl.add(reservation);

        assertThat(reservationDaoImpl.findAll()).hasSize(2);
    }

    @DisplayName("예약 내역을 삭제하는 기능을 구현한다")
    @Test
    void deleteById() {
        reservationDaoImpl.deleteById(1L);

        assertThat(reservationDaoImpl.findAll()).hasSize(0);
    }
}
