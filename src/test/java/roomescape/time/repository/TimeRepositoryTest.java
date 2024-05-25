package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.TimeFixture.TIME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.test.RepositoryTest;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;

class TimeRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_TIME = 3;

    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약 가능한 예약 시간을 조회할 수 있다.")
    @Test
    void findTimesExistsReservationDateAndThemeIdTest() {
        ReservationTime reservedTime = timeRepository.save(TIME_1);
        ReservationTime notReservedTime = timeRepository.save(TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        LocalDate date = LocalDate.now();
        reservationRepository.save(new Reservation(MEMBER_BRI, date, reservedTime, theme));

        List<ReservationTime> actual = timeRepository.findTimesExistsReservationDateAndThemeId(date, theme.getId());

        assertThat(actual).containsExactlyInAnyOrder(notReservedTime);
    }

    @DisplayName("시간이 일치하는 예약 시간이 존재하는 것을 확인할 수 있다.")
    @Test
    void existsByStartAtTrueTest() {
        timeRepository.save(TIME_1);
        boolean actual = timeRepository.existsByStartAt(TIME_1.getStartAt());

        assertThat(actual).isTrue();
    }

    @DisplayName("시간이 일치하는 예약 시간이 존재하지 않는 것을 확인할 수 있다.")
    @Test
    void existsByStartAtFalseTest() {
        boolean actual = timeRepository.existsByStartAt(LocalTime.of(1, 0));

        assertThat(actual).isFalse();
    }
}
