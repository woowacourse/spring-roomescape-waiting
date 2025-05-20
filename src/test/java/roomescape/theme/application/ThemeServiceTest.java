package roomescape.theme.application;


import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.fixture.domain.MemberFixture.notSavedMember1;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.fixture.config.TestConfig;
import roomescape.fixture.domain.ReservationTimeFixture;
import roomescape.fixture.domain.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.ui.dto.CreateThemeRequest;
import roomescape.theme.ui.dto.ThemeResponse;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 테마를_저장한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final CreateThemeRequest request = new CreateThemeRequest(name, description, thumbnail);

        // when & then
        Assertions.assertThatCode(() -> {
            themeService.create(request);
        }).doesNotThrowAnyException();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        final Long id = themeRepository.save(theme).getId();

        // when & then
        Assertions.assertThatCode(() -> themeService.delete(id))
                .doesNotThrowAnyException();
    }

    @Test
    void 테마_전체를_조회한다() {
        // given
        final String name1 = "우가우가";
        final String description1 = "우가우가 설명";
        final String thumbnail1 = "따봉우가.jpg";
        final Theme theme1 = new Theme(name1, description1, thumbnail1);
        themeRepository.save(theme1);

        final String name2 = "우가우가2";
        final String description2 = "우가우가2 설명";
        final String thumbnail2 = "따봉우가2.jpg";
        final Theme theme2 = new Theme(name2, description2, thumbnail2);
        themeRepository.save(theme2);

        // when
        final List<ThemeResponse> themes = themeService.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(themes).hasSize(2);
            softly.assertThat(themes.get(0).name()).isEqualTo(name1);
            softly.assertThat(themes.get(0).description()).isEqualTo(description1);
            softly.assertThat(themes.get(0).thumbnail()).isEqualTo(thumbnail1);
            softly.assertThat(themes.get(1).name()).isEqualTo(name2);
            softly.assertThat(themes.get(1).description()).isEqualTo(description2);
            softly.assertThat(themes.get(1).thumbnail()).isEqualTo(thumbnail2);
        });
    }

    @Test
    void 테마_이름은_중복_될_수_없다() {
        // given
        final String name = "우가우가";
        final String description = "우가우가 설명";
        final String thumbnail = "따봉우가.jpg";
        final Theme theme = new Theme(name, description, thumbnail);
        themeRepository.save(theme);

        final CreateThemeRequest request = new CreateThemeRequest(name, description, thumbnail);

        // when & then
        Assertions.assertThatThrownBy(() -> themeService.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 인기_테마_목록을_조회한다() {
        // given
        final LocalDate now = LocalDate.now();
        final LocalDate weekAgo = now.minusDays(7);

        final int themeSize = 7;
        final List<Theme> themes = ThemeFixture.notSavedThemes(themeSize);
        for (final Theme theme : themes) {
            themeRepository.save(theme);
        }

        final List<ReservationTime> times = ReservationTimeFixture.notSavedReservationTimes(11);
        for (final ReservationTime time : times) {
            reservationTimeRepository.save(time);
        }

        final Member member = memberRepository.save(notSavedMember1());

        // theme.get(i) 테마에 예약 themeCounts.get(i)개 추가
        final List<Integer> themeCounts = List.of(5, 3, 4, 6, 2);
        for (int themeIndex = 0; themeIndex < 5; themeIndex++) {
            for (int timeIndex = 0; timeIndex < themeCounts.get(themeIndex); timeIndex++) {
                reservationRepository.save(
                        new Reservation(
                                now.minusDays(themeIndex), times.get(timeIndex), themes.get(themeIndex), member,
                                ReservationStatus.CONFIRMED
                        )
                );
            }
        }

        // theme.get(5)는 weekAgo보다 이전 날짜로 예약 10개 추가 -> weekAgo~now 기간에는 예약 0개로 취급되어야 함
        for (int timeIndex = 0; timeIndex < 10; timeIndex++) {
            reservationRepository.save(
                    new Reservation(
                            weekAgo.minusDays(2), times.get(timeIndex), themes.get(5), member,
                            ReservationStatus.CONFIRMED
                    )
            );
        }

        // theme.get(6) 테마는 now날짜에 예약 1개, now보다 이후 날짜에 예약 10개 -> weekAgo~now 기간에는 예약 1개로 취급되어야 함
        for (int timeIndex = 0; timeIndex < 11; timeIndex++) {
            reservationRepository.save(
                    new Reservation(
                            now.plusDays(timeIndex), times.get(timeIndex), themes.get(6), member,
                            ReservationStatus.CONFIRMED
                    )
            );
        }

        // when
        final List<ThemeResponse> popularThemes = themeService.findPopularThemes();

        // then
        assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(themes.size());
            softly.assertThat(popularThemes.get(0).id()).isEqualTo(themes.get(3).getId());
            softly.assertThat(popularThemes.get(1).id()).isEqualTo(themes.get(0).getId());
            softly.assertThat(popularThemes.get(2).id()).isEqualTo(themes.get(2).getId());
            softly.assertThat(popularThemes.get(3).id()).isEqualTo(themes.get(1).getId());
            softly.assertThat(popularThemes.get(4).id()).isEqualTo(themes.get(4).getId());
            softly.assertThat(popularThemes.get(5).id()).isEqualTo(themes.get(6).getId());
            softly.assertThat(popularThemes.get(6).id()).isEqualTo(themes.get(5).getId());
        });
    }
}
