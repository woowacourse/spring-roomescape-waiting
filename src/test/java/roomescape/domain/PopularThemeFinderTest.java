package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.MemberFixture;
import roomescape.ThemeFixture;
import roomescape.TimeFixture;
import roomescape.application.ServiceTest;
import roomescape.config.TestConfig;
import roomescape.domain.repository.MemberCommandRepository;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeCommandRepository;
import roomescape.domain.repository.TimeQueryRepository;

@Import({TestConfig.class, PopularThemeFinder.class})
@DataJpaTest
class PopularThemeFinderTest {

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private PopularThemeFinder popularThemeFinder;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private TimeCommandRepository timeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @Autowired
    private Clock clock;

    @DisplayName("현재 날짜 이전 1주일 동안 가장 예약이 많이 된 테마 10개를 내림차순 정렬하여 조회한다.")
    @Test
    void shouldReturnThemesWhenFindPopularThemes() {
        Member member = memberCommandRepository.save(MemberFixture.defaultValue());
        LocalDate date = LocalDate.now().minusDays(1);
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme1 = themeCommandRepository.save(ThemeFixture.defaultValue());
        Theme theme2 = themeCommandRepository.save(ThemeFixture.defaultValue());
        Theme theme3 = themeCommandRepository.save(ThemeFixture.defaultValue());
        Theme theme4 = themeCommandRepository.save(ThemeFixture.defaultValue());
        reservationCommandRepository.save(createReservation(member, time, theme1));
        reservationCommandRepository.save(createReservation(member, time, theme2));
        reservationCommandRepository.save(createReservation(member, time, theme2));
        reservationCommandRepository.save(createReservation(member, time, theme2));
        reservationCommandRepository.save(createReservation(member, time, theme3));
        reservationCommandRepository.save(createReservation(member, time, theme3));

        List<Theme> popularThemes = popularThemeFinder.findThemes();

        assertThat(popularThemes).containsExactly(theme2, theme3, theme1);
    }

    private Reservation createReservation(Member member, Time time, Theme theme) {
        return new Reservation(
                member,
                LocalDate.now(clock).minusDays(1),
                time,
                theme
        );
    }
}
