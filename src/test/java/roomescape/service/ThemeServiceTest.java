package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.TestClockConfig;
import roomescape.domain.PopularTheme;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.theme.CreateThemeRequest;
import roomescape.dto.theme.ThemeReservationTimeResponse;
import roomescape.dto.theme.ThemeResponses;
import roomescape.fixture.Fixtures;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeThemeRepository;

class ThemeServiceTest {

    private FakeThemeRepository themeRepository;
    private FakeReservationRepository reservationRepository;
    private FakeReservationTimeRepository reservationTimeRepository;
    private ThemeService service;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository(reservationRepository);
        service = new ThemeService(themeRepository, reservationRepository,
                reservationTimeRepository, new TestClockConfig().timeProvider());
    }

    @Test
    void createTheme_id가_채워진_도메인을_반환한다() {
        CreateThemeRequest request = new CreateThemeRequest("공포", "무서움", "https://thumbnail.url");

        Theme created = service.createTheme(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("공포");
        assertThat(created.getDescription()).isEqualTo("무서움");
        assertThat(created.getThumbnailImageUrl()).isEqualTo("https://thumbnail.url");
    }

    @Test
    void getThemes_다음_페이지가_있으면_hasNext가_true() {
        themeRepository.save(new Theme(null, "A", "a", "u"));
        themeRepository.save(new Theme(null, "B", "b", "u"));
        themeRepository.save(new Theme(null, "C", "c", "u"));

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getThemes_다음_페이지가_없으면_hasNext가_false() {
        themeRepository.save(new Theme(null, "A", "a", "u"));
        themeRepository.save(new Theme(null, "B", "b", "u"));

        ThemeResponses responses = service.getThemes(0, 2);

        assertThat(responses.themes()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getTheme_id로_단건을_조회한다() {
        Long id = themeRepository.save(new Theme(null, "공포", "무서움", "u"));

        Theme found = service.getTheme(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getName()).isEqualTo("공포");
    }

    @Test
    void getTheme_없는_id이면_ResourceNotFoundException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.getTheme(9999L))
                .isInstanceOf(roomescape.exception.ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteTheme_없는_id이면_ResourceNotFoundException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deleteTheme(9999L))
                .isInstanceOf(roomescape.exception.ResourceNotFoundException.class)
                .hasMessageContaining("테마")
                .hasMessageContaining("9999");
    }

    @Test
    void deleteTheme_삭제후_조회되지_않는다() {
        Long id = themeRepository.save(new Theme(null, "공포", "무서움", "u"));

        service.deleteTheme(id);

        ThemeResponses responses = service.getThemes(0, 10);
        assertThat(responses.themes()).extracting("id").doesNotContain(id);
    }

    @Test
    void getThemeTimes_예약된_시간은_isReserved가_true_나머지는_false() {
        Long themeId = themeRepository.save(new Theme(null, "공포", "무서움", "u"));
        Long time1 = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long time2 = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        reservationRepository.save(buildReservation("브라운", themeId, time1, LocalDate.of(2026, 5, 6)));

        List<ThemeReservationTimeResponse> times =
                service.getThemeTimes(themeId, LocalDate.of(2026, 5, 6));

        assertThat(times).hasSize(2);
        ThemeReservationTimeResponse t1 = times.stream()
                .filter(t -> t.id().equals(time1)).findFirst().orElseThrow();
        ThemeReservationTimeResponse t2 = times.stream()
                .filter(t -> t.id().equals(time2)).findFirst().orElseThrow();
        assertThat(t1.isReserved()).isTrue();
        assertThat(t2.isReserved()).isFalse();
    }

    @Test
    void getPopularThemes_today_minus1_부터_today_minus7_까지의_예약만_집계된다() {
        Long timeId = reservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long themeIn = themeRepository.save(new Theme(null, "기간내", "in", "u"));
        Long themeOut = themeRepository.save(new Theme(null, "기간외", "out", "u"));

        reservationRepository.save(buildReservation("a", themeIn, timeId, LocalDate.of(2026, 4, 30)));  // 시작 경계
        reservationRepository.save(buildReservation("b", themeIn, timeId, LocalDate.of(2026, 5, 6)));   // 끝 경계
        reservationRepository.save(buildReservation("c", themeOut, timeId, LocalDate.of(2026, 4, 29))); // 시작 직전
        reservationRepository.save(buildReservation("d", themeOut, timeId, LocalDate.of(2026, 5, 7)));  // today

        List<PopularTheme> popular = service.getPopularThemes(10);

        assertThat(popular).extracting(p -> p.getTheme().getId()).containsExactly(themeIn);
        assertThat(popular).extracting(PopularTheme::getReservationCount).containsExactly(2L);
    }

    private Reservation buildReservation(String name, Long themeId, Long timeId, LocalDate date) {
        User user = Fixtures.memberWithId(1L, name);
        Theme theme = themeRepository.findById(themeId).orElseThrow();
        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow();
        return Fixtures.reservation(user, theme, date, time);
    }
}
