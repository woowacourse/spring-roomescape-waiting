package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.model.Member;
import roomescape.member.model.Role;
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
    void DataSource_접근_테스트() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null)
                .next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 예약을_추가할_수_있다() {
        Member member = new Member(1L, "일반1", "normal1@normal.com", "password", Role.NORMAL);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨1", "탈출하기", "http/~");
        Reservation reservation = new Reservation(null, member, LocalDate.of(2024, 4, 22), time,
                theme);

        Reservation newReservation = jdbcReservationDao.add(reservation);

        assertThat(newReservation).isNotNull();
    }

    @Test
    void 전체_예약_기록을_조회할_수_있다() {
        List<Reservation> reservations = jdbcReservationDao.findAll();

        assertThat(reservations).hasSize(21);
    }

    @Test
    void 특정_날짜와_테마에_해당하는_예약을_조회할_수_있다() {
        // Given
        List<Reservation> reservations = jdbcReservationDao.findByDateAndThemeId(LocalDate.of(2025, 5, 1), 1L);

        // When & Then
        assertThat(reservations).hasSize(2);
    }

    @Test
    void 멤버와_테마_날짜로_필터링하여_검색할_수_있다() {
        // Given
        // When
        // Then
        assertAll(() -> {
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(1L, null, null, null)).hasSize(10);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(null, 1L, null, null)).hasSize(10);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(null, null, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(12);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(1L, 1L, null, null)).hasSize(5);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(1L, null, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(6);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(null, 1L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(6);
            assertThat(jdbcReservationDao.findByMemberIdAndThemeIdAndStartDateAndEndDate(1L, 1L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(3);
        });
    }

    @Test
    void ID로_예약을_삭제할_수_있다() {
        Member member = new Member(1L, "일반1", "normal1@normal.com", "password", Role.NORMAL);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨4", "탈출하기", "http/~");
        Reservation reservation = new Reservation(null, member, LocalDate.of(2024, 4, 22), time,
            theme);

        Reservation newReservation = jdbcReservationDao.add(reservation);
        assertThatCode(() -> jdbcReservationDao.deleteById(newReservation.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    void 날짜_시간_테마에_해당되는_예약이_존재하는지_확인할_수_있다() {
        Member member = new Member(1L, "일반1", "normal1@normal.com", "password", Role.NORMAL);
        LocalDate date = LocalDate.of(2024, 12, 31);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "레벨4", "탈출하기", "http/~");

        Reservation reservation = new Reservation(null, member, date, time, theme);
        jdbcReservationDao.add(reservation);

        assertThat(jdbcReservationDao.existByDateTimeAndTheme(date, 1L, 1L)).isTrue();
    }
}
