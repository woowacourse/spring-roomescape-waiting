package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.time.fixture.DateTimeFixture.DAY_AFTER_TOMORROW;
import static roomescape.time.fixture.DateTimeFixture.TOMORROW;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.fixture.MemberFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

// 테스트 초기 데이터
// 예약1: {ID=1, MEMBER=썬, DATE=내일, TIME={ID=1, START_AT=10:00}, THEME={ID=1, NAME=잠실 캠퍼스 탈출}}
// 예약2: {ID=2, MEMBER=리비, DATE=내일, TIME={ID=2, START_AT=11:00}, THEME={ID=1, NAME=잠실 캠퍼스 탈출}}
// 예약3: {ID=3, MEMBER=도도 , DATE=내일, TIME={ID=2, START_AT=10:00}, THEME={ID=2, NAME=선릉 캠퍼스 탈출}}
@JdbcTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @DisplayName("예약 데이터를 모두 가져올 수 있다")
    @Test
    void should_findAll() {
        assertThat(reservationRepository.findAll()).hasSize(3);
    }


    @DisplayName("원하는 ID의 예약을 불러올 수 있다")
    @Test
    void should_findById() {
        assertThat(reservationRepository.findById(1L)).isPresent();
    }

    @DisplayName("예약을 추가할 수 있다")
    @Test
    void should_insert() {
        ReservationTime reservationTime = new ReservationTime(3L, LocalTime.of(10, 0));
        Theme theme = new Theme(2L,
                "잠실 캠퍼스 탈출",
                "미션을 빨리 진행하고 잠실 캠퍼스를 탈출하자!",
                "https://velog.velcdn.com/images/jangws/post/cfe0e548-1242-470d-bfa8-19eeb72debc5/image.jpg");

        Reservation reservation = new Reservation(null, MemberFixture.MEMBER_ID_1, DAY_AFTER_TOMORROW, reservationTime,
                theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        assertThat(savedReservation.getId()).isNotNull();
    }

    @DisplayName("원하는 ID의 예약을 삭제할 수 있다")
    @Test
    void should_deleteById() {
        reservationRepository.deleteById(1L);
        assertThat(reservationRepository.findAll()).hasSize(2);
    }

    @DisplayName("예약 간 예약날짜와 예약 시간 ID와 테마 ID가 동일한 경우를 알 수 있다")
    @Test
    void should_return_true_when_reservation_date_and_time_id_and_theme_id_equal() {
        assertThat(reservationRepository.existByDateAndTimeIdAndThemeId(TOMORROW, 1L, 1L)).isTrue();
    }

    @DisplayName("예약날짜와 예약 시간 ID와 테마 ID가 동일하지 않은 경우를 알 수 있다")
    @Test
    void should_return_false_when_reservation_date_and_time_id_and_theme_id_not_equal() {
        assertThat(reservationRepository.existByDateAndTimeIdAndThemeId(DAY_AFTER_TOMORROW, 2L, 3L)).isFalse();
    }
}
