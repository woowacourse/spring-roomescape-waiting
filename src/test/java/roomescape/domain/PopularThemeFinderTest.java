package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;

@ServiceTest
public class PopularThemeFinderTest {

    @Autowired
    private PopularThemeFinder popularThemeFinder;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private Clock clock;

    @DisplayName("현재 날짜 이전 1주일 동안 가장 예약이 많이 된 테마 10개를 내림차순 정렬하여 조회한다.")
    @Test
    void shouldReturnThemesWhenFindPopularThemes() {
        ReservationTime reservationTime1 = reservationTimeRepository.findById(1L).orElseThrow();
        ReservationTime reservationTime2 = reservationTimeRepository.findById(2L).orElseThrow();
        ReservationTime reservationTime3 = reservationTimeRepository.findById(3L).orElseThrow();
        Theme theme1 = themeRepository.create(ThemeFixture.of("test1", "test1", "test1"));
        Theme theme2 = themeRepository.create(ThemeFixture.of("test2", "test2", "test2"));
        Theme theme3 = themeRepository.create(ThemeFixture.of("test3", "test3", "test3"));
        Member member = memberCommandRepository.create(MemberFixture.defaultValue());
        reservationCommandRepository.create(createReservation(member, reservationTime1, theme1));
        reservationCommandRepository.create(createReservation(member, reservationTime2, theme2));
        reservationCommandRepository.create(createReservation(member, reservationTime3, theme2));

        List<Theme> popularThemes = popularThemeFinder.findThemes();

        assertThat(popularThemes).containsExactly(theme2, theme1);
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
