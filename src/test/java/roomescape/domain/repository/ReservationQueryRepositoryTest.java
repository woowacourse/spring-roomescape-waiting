package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.MemberFixture;
import roomescape.ThemeFixture;
import roomescape.TimeFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.dto.AvailableTimeDto;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DataJpaTest
class ReservationQueryRepositoryTest {

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private TimeCommandRepository timeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @BeforeEach
    void setUp() {
        LocalDate date = LocalDate.now();
        Member member = memberCommandRepository.save(MemberFixture.defaultValue());
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme = themeCommandRepository.save(ThemeFixture.defaultValue());
        reservationCommandRepository.save(new Reservation(member, date, time, theme));
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void findAllTest() {
        List<Reservation> reservations = reservationQueryRepository.findAll();

        assertThat(reservations).hasSize(1);
    }

    @DisplayName("특정 날짜와 테마로 예약 가능한 시간을 조회한다.")
    @Test
    void findAvailableTimeTest() {
        List<AvailableTimeDto> availableTimes = reservationQueryRepository.findAvailableReservationTimes(
                LocalDate.now(), 1L);

        assertThat(availableTimes.size()).isOne();
    }

    @DisplayName("시작 날짜와 끝 날짜 사이에 예약 중 인기있는 테마를 조회한다.")
    @Test
    void findPopularThemesDateBetweenTest() {
        LocalDate now = LocalDate.now();
        List<Theme> popularThemes = reservationQueryRepository.findPopularThemesDateBetween(now, now);
        List<Long> ids = popularThemes.stream()
                .map(Theme::getId)
                .toList();

        assertThat(ids).containsExactly(1L);
    }

    @DisplayName("회원의 id, 시작날짜, 끝날짜, 테마에 해당하는 예약을 조회한다.")
    @Test
    void findByCriteriaTest() {
        LocalDate now = LocalDate.now();
        List<Reservation> reservations = reservationQueryRepository.findByCriteria(null, now, now, null);

        assertThat(reservations).isNotEmpty();
    }

    @DisplayName("회원의 id, 시작날짜, 끝날짜, 테마에 해당하는 예약이 없다면 빈 리스트를 반환한다.")
    @Test
    void findByCriteriaTest2() {
        List<Reservation> reservations = reservationQueryRepository.findByCriteria(null, null, null, 99L);

        assertThat(reservations.size()).isZero();
    }

    @DisplayName("존재하지 않는 예약 id로 조회시 예외가 발생한다.")
    @Test
    void getByIdExceptionTest() {
        assertThatCode(() -> reservationQueryRepository.getById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_RESERVATION);
    }

    @DisplayName("존재하지 않는 날짜, 시간, 테마로 이루어진 예약 조회시 예외가 발생한다.")
    @Test
    void getByDateAndTimeAndThemeExceptionTest() {
        assertThatCode(() -> reservationQueryRepository.getByDateAndTimeAndTheme(LocalDate.now(), null, null))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_RESERVATION);
    }
}
