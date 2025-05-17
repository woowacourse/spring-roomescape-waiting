package roomescape.theme.infrastructure;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.fixture.config.TestConfig;
import roomescape.fixture.domain.ReservationTimeFixture;
import roomescape.fixture.domain.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeQueryRepository;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ThemeRepositoryImplTest {

    @Autowired
    private ThemeRepositoryImpl themeRepositoryImpl;

    @Autowired
    private ThemeQueryRepository themeQueryRepository;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationTimeCommandRepository reservationTimeCommandRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;
    
    @Test
    void 특정_기간_사이에_예약이_많은_n개의_테마_목록을_내림차순으로_반환한다() {
        // given
        final LocalDate now = LocalDate.now();
        final LocalDate weekAgo = now.minusDays(7);

        final int themeSize = 7;
        final List<Theme> themes = ThemeFixture.notSavedThemes(themeSize);
        for (Theme theme : themes) {
            themeRepositoryImpl.save(theme);
        }

        final List<ReservationTime> times = ReservationTimeFixture.notSavedReservationTimes(11);
        for (ReservationTime time : times) {
            reservationTimeCommandRepository.save(time);
        }

        final Member member = memberCommandRepository.save(NOT_SAVED_MEMBER_1());

        // theme.get(i) 테마에 예약 themeCounts.get(i)개 추가
        final List<Integer> themeCounts = List.of(5, 3, 4, 6, 2);
        for (int themeIndex = 0; themeIndex < 5; themeIndex++) {
            for (int timeIndex = 0; timeIndex < themeCounts.get(themeIndex); timeIndex++) {
                reservationCommandRepository.save(
                        new Reservation(
                                now.minusDays(themeIndex), times.get(timeIndex), themes.get(themeIndex), member,
                                ReservationStatus.CONFIRMED
                        )
                );
            }
        }

        // theme.get(5)는 weekAgo보다 이전 날짜로 예약 10개 추가 -> weekAgo~now 기간에는 예약 0개로 취급되어야 함
        for (int timeIndex = 0; timeIndex < 10; timeIndex++) {
            reservationCommandRepository.save(
                    new Reservation(
                            weekAgo.minusDays(2), times.get(timeIndex), themes.get(5), member,
                            ReservationStatus.CONFIRMED
                    )
            );
        }

        // theme.get(6) 테마는 now날짜에 예약 1개, now보다 이후 날짜에 예약 10개 -> weekAgo~now 기간에는 예약 1개로 취급되어야 함
        for (int timeIndex = 0; timeIndex < 11; timeIndex++) {
            reservationCommandRepository.save(
                    new Reservation(
                            now.plusDays(timeIndex), times.get(timeIndex), themes.get(6), member,
                            ReservationStatus.CONFIRMED
                    )
            );
        }

        // when
        final List<Theme> popularThemes =
                themeQueryRepository.findTopNThemesByReservationCountInDateRange(weekAgo, now, 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(themes.size());
            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(themes.get(3).getId());
            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(themes.get(0).getId());
            softly.assertThat(popularThemes.get(2).getId()).isEqualTo(themes.get(2).getId());
            softly.assertThat(popularThemes.get(3).getId()).isEqualTo(themes.get(1).getId());
            softly.assertThat(popularThemes.get(4).getId()).isEqualTo(themes.get(4).getId());
            softly.assertThat(popularThemes.get(5).getId()).isEqualTo(themes.get(6).getId());
            softly.assertThat(popularThemes.get(6).getId()).isEqualTo(themes.get(5).getId());
        });
    }
}
