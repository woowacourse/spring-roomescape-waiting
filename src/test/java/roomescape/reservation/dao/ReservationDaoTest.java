package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSearch;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@JdbcTest
@Sql(scripts = "/init-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationDaoTest {
    private static final int COUNT_OF_RESERVATION = 4;
    private static final Reservation EXISTED_RESERVATION = new Reservation(
            new Member(2L, "브라운", "brown@abc.com"),
            LocalDate.of(2022, 5, 5),
            new ReservationTime(2L, LocalTime.of(19, 0)),
            new Theme(1L, "레벨2 탈출", "레벨2 탈출하기", "https://img.jpg"));

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        reservationDao = new ReservationDao(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @DisplayName("모든 예약을 읽을 수 있다.")
    @Test
    void findReservationsTest() {
        List<Reservation> reservations = reservationDao.findReservations();

        assertThat(reservations).hasSize(COUNT_OF_RESERVATION);
    }

    @DisplayName("조건에 따라 예약을 읽을 수 있다.")
    @Test
    void findReservationsTest_whenUsingSearchCondition() {
        ReservationSearch searchCondition = new ReservationSearch(
                1L, 2L, LocalDate.of(2022, 5, 4), LocalDate.of(2022, 5, 6));

        List<Reservation> actual = reservationDao.findReservations(searchCondition);

        assertThat(actual).hasSize(1);
    }

    @DisplayName("일부 조건에 따라 예약을 읽을 수 있다.")
    @Test
    void findReservationsTest_whenUsingSearchSomeCondition() {
        ReservationSearch searchCondition = new ReservationSearch(
                null, 2L, null, LocalDate.now());

        List<Reservation> actual = reservationDao.findReservations(searchCondition);

        assertThat(actual).hasSize(2);
    }

    @DisplayName("예약을 생성할 수 있다.")
    @Test
    void createReservationTest() {
        long expectedId = COUNT_OF_RESERVATION + 1;
        Reservation reservation = new Reservation(
                new Member(4L, "오리", "duck@abc.com"),
                LocalDate.of(2024, 8, 15),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(3L, "레벨4 탈출", "레벨4 탈출하기",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));

        Reservation actual = reservationDao.createReservation(reservation);

        assertThat(actual.getId()).isEqualTo(expectedId);
    }

    @DisplayName("날짜, 시간, 테마가 모두 겹치는 예약이 있다면, 예약을 생성할 수 없다.")
    @Test
    void createReservationTest_whenReservationIsDuplicated() {
        assertThatThrownBy(() -> reservationDao.createReservation(EXISTED_RESERVATION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 날짜, 시간, 테마에 이미 예약이 존재합니다.");
    }

    @DisplayName("예약을 삭제할 수 있다.")
    @Test
    void deleteReservationTest() {
        reservationDao.deleteReservation(1L);

        assertThat(countSavedReservation()).isEqualTo(COUNT_OF_RESERVATION - 1);
    }

    private int countSavedReservation() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM reservation", Integer.class);
    }
}
