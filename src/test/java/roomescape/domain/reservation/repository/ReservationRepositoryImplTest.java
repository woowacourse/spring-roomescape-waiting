package roomescape.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.LocalDateFixture.AFTER_ONE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.AFTER_THREE_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.AFTER_TWO_DAYS_DATE;
import static roomescape.fixture.LocalDateFixture.TODAY;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;
import static roomescape.fixture.MemberFixture.NULL_ID_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.NULL_ID_RESERVATION_TIME;
import static roomescape.fixture.ReservationTimeFixture.TEN_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.DUMMY_THEME;
import static roomescape.fixture.ThemeFixture.NULL_ID_DUMMY_THEME;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;
import roomescape.domain.member.repository.JpaMemberRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.repository.reservation.JpaReservationRepository;
import roomescape.domain.reservation.repository.reservation.ReservationRepositoryImpl;
import roomescape.domain.reservation.repository.reservationTime.JpaReservationTimeRepository;
import roomescape.domain.theme.repository.JpaThemeRepository;

class ReservationRepositoryImplTest extends RepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    private ReservationRepositoryImpl reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new ReservationRepositoryImpl(jpaReservationRepository);
        jpaMemberRepository.save(NULL_ID_MEMBER);
        jpaThemeRepository.save(NULL_ID_DUMMY_THEME);
        jpaReservationTimeRepository.save(NULL_ID_RESERVATION_TIME);
        jpaReservationRepository.save(
                new Reservation(null, AFTER_TWO_DAYS_DATE, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER));
    }

    @AfterEach
    void setDown() {
        jpaReservationRepository.deleteAll();
    }

    @DisplayName("필터링되어 예약이 존재하는 예약 목록을 불러옵니다.")
    @Test
    void should_exist_filtering_reservation_result() {
        int expectSize = 1;

        int actualSize = reservationRepository.findAllBy(1L, 1L, TODAY, AFTER_TWO_DAYS_DATE).size();

        assertThat(actualSize).isEqualTo(expectSize);
    }

    @DisplayName("필터링되어 예약이 존재하지않는 예약 목록을 불러옵니다.")
    @Test
    void should_not_exist_filtering_reservation_result() {
        int expectSize = 0;

        int actualSize = reservationRepository.findAllBy(1L, 1L, TODAY, AFTER_ONE_DAYS_DATE).size();

        assertThat(actualSize).isEqualTo(expectSize);
    }

    @DisplayName("예약날짜와 예약 시간 ID와 테마 ID가 동일한 경우를 알 수 있습니다.")
    @Test
    void should_return_true_when_reservation_date_and_time_id_and_theme_id_equal() {
        assertThat(reservationRepository.existByDateAndTimeIdAndThemeId(AFTER_TWO_DAYS_DATE, 1L, 1L)).isTrue();
    }

    @DisplayName("예약날짜와 예약 시간 ID와 테마 ID가 동일하지 않은 경우를 알 수 있습니다.")
    @Test
    void should_return_false_when_reservation_date_and_time_id_and_theme_id_not_equal() {
        assertThat(reservationRepository.existByDateAndTimeIdAndThemeId(AFTER_THREE_DAYS_DATE, 1L, 1L)).isFalse();
    }

    @DisplayName("예약날짜와 테마Id로 예약목록을 불러올 수 있습니다.")
    @Test
    void should_find_reservations_by_date_and_theme_id() {
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(AFTER_TWO_DAYS_DATE, 1L);

        assertThat(reservations).hasSize(1);
    }

    @DisplayName("멤버 ID로 예약목록을 불러올 수 있습니다.")
    @Test
    void should_find_reservations_by_member_id() {
        List<Reservation> reservations = reservationRepository.findByMemberId(1L);

        assertThat(reservations).hasSize(1);
    }
}
