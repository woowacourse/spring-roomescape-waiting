package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.dao.JdbcReservationDao;

@JdbcTest
@Import(JdbcReservationDao.class)
public class JdbcReservationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JdbcReservationDao jdbcReservationDao;

    @Test
    @DisplayName("DataSource 접근 테스트")
    void connectJdbc() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null)
                .next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void add() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨1", "탈출하기", "http/~");
        Reservation reservation = new Reservation(null, "사나", LocalDate.of(2024, 4, 22), time,
                theme);

        Reservation newReservation = jdbcReservationDao.add(reservation);

        assertThat(newReservation).isNotNull();
    }

    @Test
    @DisplayName("전체 예약 기록을 조회할 수 있다.")
    void findAll() {
        List<Reservation> reservations = jdbcReservationDao.findAll();

        assertThat(reservations).hasSize(3);
    }

    @Test
    @DisplayName("특정 날짜와 테마에 해당하는 예약을 조회할 수 있다.")
    void findByDateAndThemeId() {
        // Given
        List<Reservation> reservations = jdbcReservationDao.findByDateAndThemeId(LocalDate.of(2025, 4, 28), 1L);

        // When & Then
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("ID로 예약을 삭제할 수 있다.")
    void deleteById() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨4", "탈출하기", "http/~");
        Reservation reservation = new Reservation(null, "사나", LocalDate.of(2024, 4, 22), time,
            theme);

        Reservation newReservation = jdbcReservationDao.add(reservation);
        assertThatCode(() -> jdbcReservationDao.deleteById(newReservation.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("날짜, 시간, 테마에 해당되는 예약이 존재하는지 확인할 수 있다.")
    void existByDateTimeAndTheme() {
        LocalDate date = LocalDate.of(2024, 12, 31);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨4", "탈출하기", "http/~");

        Reservation reservation = new Reservation(null, "사나", date, time, theme);
        jdbcReservationDao.add(reservation);

        assertThat(jdbcReservationDao.existByDateTimeAndTheme(date, 1L, 1L)).isTrue();
    }
}
