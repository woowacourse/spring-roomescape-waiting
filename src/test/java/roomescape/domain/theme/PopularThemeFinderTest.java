package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Status;

@ServiceTest
class PopularThemeFinderTest {
    @Autowired
    private PopularThemeFinder popularThemeFinder;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private Clock clock;

    @DisplayName("현재 날짜 이전 1주일 동안 가장 예약이 많이 된 테마 10개를 내림차순 정렬하여 조회한다.")
    @Test
    void shouldReturnThemesWhenFindPopularThemes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Theme> themes = themeRepository.findAll();
        Member member = memberRepository.findAll().get(0);
        reservationRepository.save(createReservation(member, reservationTimes.get(0), themes.get(0)));
        reservationRepository.save(createReservation(member, reservationTimes.get(1), themes.get(1)));
        reservationRepository.save(createReservation(member, reservationTimes.get(0), themes.get(1)));

        List<Theme> popularThemes = popularThemeFinder.findThemes();

        assertThat(popularThemes).containsExactly(themes.get(1), themes.get(0));
    }

    private Reservation createReservation(Member member, ReservationTime reservationTime, Theme theme) {
        return new Reservation(
                member,
                LocalDate.now(clock).minusDays(1),
                reservationTime,
                theme,
                Status.RESERVATION
        );
    }
}
