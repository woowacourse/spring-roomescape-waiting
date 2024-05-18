package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationTimeCommandRepository;
import roomescape.domain.repository.ThemeCommandRepository;

@ServiceTest
class PopularThemeFinderTest {

    @Autowired
    private PopularThemeFinder popularThemeFinder;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationTimeCommandRepository reservationTimeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @Autowired
    private Clock clock;

    @DisplayName("현재 날짜 이전 1주일 동안 가장 예약이 많이 된 테마 10개를 내림차순 정렬하여 조회한다.")
    @Test
    void shouldReturnThemesWhenFindPopularThemes() {
        List<ReservationTime> reservationTimes = reservationTimeCommandRepository.findAll();
        List<Theme> themes = themeCommandRepository.findAll();
        Member member = memberQueryRepository.findAll().get(0);
        reservationCommandRepository.save(createReservation(member, reservationTimes.get(0), themes.get(0)));
        reservationCommandRepository.save(createReservation(member, reservationTimes.get(1), themes.get(1)));
        reservationCommandRepository.save(createReservation(member, reservationTimes.get(1), themes.get(1)));

        List<Theme> popularThemes = popularThemeFinder.findThemes();

        assertThat(popularThemes).containsExactly(themes.get(1), themes.get(0));
    }

    private Reservation createReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(
                member,
                LocalDate.now(clock).minusDays(1),
                reservationTime,
                theme
        );
    }
}
