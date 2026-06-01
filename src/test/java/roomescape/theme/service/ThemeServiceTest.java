package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeReservationQueryRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.theme.application.dto.PopularThemeQueryResult;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.domain.repository.PopularTheme;

public class ThemeServiceTest {

    private FakeThemeRepository themeRepository;
    private ReservationQueryService reservationQueryService;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        reservationQueryService = new ReservationQueryService(
                new FakeReservationQueryRepository(
                        new FakeReservationRepository(themeRepository, new FakeReservationTimeRepository())
                )
        );
        themeService = new ThemeService(themeRepository, reservationQueryService);
    }

    @DisplayName("테마의 정상 추가를 테스트합니다.")
    @Test
    void save_theme_successfully() {
        ThemeCreateCommand createRequestDto = new ThemeCreateCommand("theme name", "theme description",
                "theme img url");
        ThemeQueryResult themeQueryResult = themeService.save(createRequestDto);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(themeQueryResult.id()).isEqualTo(1L);
            assertSoftly.assertThat(themeQueryResult.name()).isEqualTo("theme name");
            assertSoftly.assertThat(themeQueryResult.description()).isEqualTo("theme description");
            assertSoftly.assertThat(themeQueryResult.thumbnailImgUrl()).isEqualTo("theme img url");
        });
    }

    @DisplayName("중복된 테마 추가 시 예외 발생을 테스트합니다.")
    @Test
    void save_duplicated_theme_exception() {
        ThemeCreateCommand createRequestDto = new ThemeCreateCommand("theme name", "theme description",
                "theme img url");
        themeService.save(createRequestDto);

        assertThatThrownBy(() -> themeService.save(createRequestDto))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름과 설명이 같은 테마가 이미 존재합니다.");
    }

    @DisplayName("테마의 삭제를 테스트합니다.")
    @Test
    void delete_theme() {
        ThemeCreateCommand createRequestDto = new ThemeCreateCommand("theme name", "theme description",
                "theme img url");
        themeService.save(createRequestDto);

        assertThat(themeService.delete(1L)).isEqualTo(1);
    }

    @DisplayName("테마 조회를 테스트합니다.")
    @Test
    void find_theme() {
        ThemeCreateCommand createRequestDto = new ThemeCreateCommand("theme name", "theme description",
                "theme img url");
        themeService.save(createRequestDto);

        ThemeQueryResult foundTheme = themeService.findById(1L);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(foundTheme.id()).isEqualTo(1L);
            assertSoftly.assertThat(foundTheme.name()).isEqualTo("theme name");
            assertSoftly.assertThat(foundTheme.description()).isEqualTo("theme description");
            assertSoftly.assertThat(foundTheme.thumbnailImgUrl()).isEqualTo("theme img url");
        });
    }

    @DisplayName("존재하지 않는 테마 조회 시 예외 발생을 테스트합니다.")
    @Test
    void theme_not_exists() {
        assertThatThrownBy(() -> themeService.findById(100L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("존재하지 않는 테마 입니다.");
    }

    @DisplayName("테마의 전체 조회를 테스트합니다.")
    @Test
    void find_all_themes() {
        ThemeCreateCommand createRequestDto1 = new ThemeCreateCommand("theme name1", "theme description1",
                "theme img url1");
        ThemeCreateCommand createRequestDto2 = new ThemeCreateCommand("theme name2", "theme description2",
                "theme img url2");
        ThemeCreateCommand createRequestDto3 = new ThemeCreateCommand("theme name3", "theme description3",
                "theme img url3");

        themeService.save(createRequestDto1);
        themeService.save(createRequestDto2);
        themeService.save(createRequestDto3);

        List<ThemeQueryResult> themeRespons = themeService.findAll();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(themeRespons.size()).isEqualTo(3);
            assertSoftly.assertThat(themeRespons).containsExactly(
                    new ThemeQueryResult(1L, "theme name1", "theme description1", "theme img url1"),
                    new ThemeQueryResult(2L, "theme name2", "theme description2", "theme img url2"),
                    new ThemeQueryResult(3L, "theme name3", "theme description3", "theme img url3")
            );
        });
    }

    @DisplayName("인기 테마 조회를 테스트합니다.")
    @Test
    void find_popular_themes() {
        themeRepository.savePopularTheme(new PopularTheme(
                1L,
                "theme name",
                "theme description",
                "theme img url",
                10
        ));

        List<PopularThemeQueryResult> responses = themeService.findPopularThemes(LocalDate.of(2026, 5, 6));

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(themeRepository.getFrom()).isEqualTo(LocalDate.of(2026, 4, 29));
            assertSoftly.assertThat(themeRepository.getTo()).isEqualTo(LocalDate.of(2026, 5, 5));
            assertSoftly.assertThat(responses).containsExactly(
                    new PopularThemeQueryResult(1L, "theme name", "theme description", "theme img url", 10)
            );
        });
    }
}
