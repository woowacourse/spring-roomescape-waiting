package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ThemeServiceTest extends BaseServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("모든 테마들을 조회한다.")
    void getAllThemes() {
        Theme theme1 = themeRepository.save(ThemeFixture.create("테마1"));
        Theme theme2 = themeRepository.save(ThemeFixture.create("테마2"));

        List<ThemeResponse> themeResponses = themeService.getAllThemes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponses).hasSize(2);
            softly.assertThat(themeResponses.get(0).id()).isEqualTo(theme1.getId());
            softly.assertThat(themeResponses.get(1).id()).isEqualTo(theme2.getId());
        });
    }

    @Test
    @DisplayName("테마를 추가한다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("테마", "테마 설명", "https://example.com");

        ThemeResponse themeResponse = themeService.addTheme(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponse.name()).isEqualTo("테마");
            softly.assertThat(themeResponse.description()).isEqualTo("테마 설명");
            softly.assertThat(themeResponse.thumbnail()).isEqualTo("https://example.com");
        });
    }

    @Test
    @DisplayName("id로 테마를 삭제한다.")
    void deleteThemeById() {
        Theme theme = themeRepository.save(ThemeFixture.create());
        long id = theme.getId();

        themeService.deleteThemeById(id);

        assertThat(themeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("인기있는 테마를 내림차순으로 3개 조회한다.")
    void getPopularThemes() {
        Member member = memberRepository.save(MemberFixture.user());
        ReservationTime time = reservationTimeRepository.save(ReservationTimeFixture.create());
        Theme theme1 = themeRepository.save(ThemeFixture.create("우주 탐험"));
        Theme theme2 = themeRepository.save(ThemeFixture.create("시간여행"));
        Theme theme3 = themeRepository.save(ThemeFixture.create("마법의 숲"));
        reservationRepository.save(ReservationFixture.create("2024-04-04", member, time, theme1));
        reservationRepository.save(ReservationFixture.create("2024-04-05", member, time, theme1));
        reservationRepository.save(ReservationFixture.create("2024-04-06", member, time, theme1));
        reservationRepository.save(ReservationFixture.create("2024-04-07", member, time, theme2));
        reservationRepository.save(ReservationFixture.create("2024-04-08", member, time, theme2));
        reservationRepository.save(ReservationFixture.create("2024-04-11", member, time, theme2));
        reservationRepository.save(ReservationFixture.create("2024-04-08", member, time, theme3));
        reservationRepository.save(ReservationFixture.create("2024-04-09", member, time, theme3));
        reservationRepository.save(ReservationFixture.create("2024-04-10", member, time, theme3));

        LocalDate stateDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 10);
        int limit = 3;
        List<ThemeResponse> popularThemes = themeService.getPopularThemes(stateDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);
            softly.assertThat(popularThemes.get(0).id()).isEqualTo(3);
            softly.assertThat(popularThemes.get(0).name()).isEqualTo("마법의 숲");
            softly.assertThat(popularThemes.get(1).id()).isEqualTo(2);
            softly.assertThat(popularThemes.get(1).name()).isEqualTo("시간여행");
            softly.assertThat(popularThemes.get(2).id()).isEqualTo(1);
            softly.assertThat(popularThemes.get(2).name()).isEqualTo("우주 탐험");
        });
    }
}
