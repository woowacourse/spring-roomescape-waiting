package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.JpaReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.domain.theme.admin.dto.AdminThemeResponse;
import roomescape.domain.theme.admin.dto.CreateThemeRequest;
import roomescape.domain.theme.admin.dto.CreateThemeResponse;
import roomescape.domain.theme.dto.ThemeRankResponse;
import roomescape.domain.theme.dto.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private JpaThemeRepository themeRepository;

    @Mock
    private JpaReservationSlotRepository reservationSlotRepository;

    @Mock
    private JpaReservationRepository reservationRepository;

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-27T00:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(
            themeRepository,
            reservationSlotRepository,
            reservationRepository,
            clock
        );
    }

    @Test
    @DisplayName("관리자용 테마 목록을 조회한다.")
    void getThemeListForAdmin() {
        // given
        given(themeRepository.findAll()).willReturn(List.of(
            Theme.of(1L, "미스터리", "보예의 미스터리", "theme-url")
        ));

        // when
        List<AdminThemeResponse> responses = themeService.getAllThemeForAdmin();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().id()).isEqualTo(1L);
            assertThat(responses.getFirst().name()).isEqualTo("미스터리");
            assertThat(responses.getFirst().content()).isEqualTo("보예의 미스터리");
            assertThat(responses.getFirst().url()).isEqualTo("theme-url");
        });
    }

    @Test
    @DisplayName("사용자용 테마 목록을 조회한다.")
    void getThemeListForUser() {
        // given
        given(themeRepository.findAll()).willReturn(List.of(
            Theme.of(1L, "미스터리", "보예의 미스터리", "theme-url")
        ));

        // when
        List<ThemeResponse> responses = themeService.getAllTheme();

        // then
        assertSoftly(softly -> {
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().id()).isEqualTo(1L);
            assertThat(responses.getFirst().name()).isEqualTo("미스터리");
            assertThat(responses.getFirst().content()).isEqualTo("보예의 미스터리");
            assertThat(responses.getFirst().url()).isEqualTo("theme-url");
        });
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        Theme savedTheme = Theme.of(1L, "미스터리", "보예의 미스터리", "theme-url");
        given(themeRepository.save(any(Theme.class))).willReturn(savedTheme);

        // when
        CreateThemeResponse response = themeService.createTheme(
            new CreateThemeRequest("미스터리", "보예의 미스터리", "theme-url")
        );

        // then
        assertSoftly(softly -> {
            assertThat(response.id()).isEqualTo(savedTheme.getId());
            assertThat(response.name()).isEqualTo("미스터리");
            assertThat(response.content()).isEqualTo("보예의 미스터리");
            assertThat(response.url()).isEqualTo("theme-url");
        });
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteTheme() {
        // given
        Long themeId = 1L;
        given(reservationSlotRepository.countByThemeId(themeId)).willReturn(0);

        // when
        themeService.deleteTheme(themeId);

        // then
        verify(themeRepository).deleteById(themeId);
    }

    @Test
    @DisplayName("예약된 테마 목록으로 인기 테마 순위를 계산한다.")
    void getThemeRank() {
        // given
        Theme firstTheme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Theme secondTheme = Theme.of(2L, "추리", "추리 테마", "mystery-url");
        Theme thirdTheme = Theme.of(3L, "잠입", "잠입 테마", "escape-url");
        given(reservationRepository.findThemesForRanking(
            LocalDate.of(2026, 5, 20),
            LocalDate.of(2026, 5, 27),
            ReservationStatus.CANCELED
        )).willReturn(List.of(
            secondTheme,
            firstTheme,
            firstTheme,
            thirdTheme,
            thirdTheme
        ));

        // when
        List<ThemeRankResponse> responses = themeService.getThemeRank();

        // then
        assertThat(responses)
            .extracting(
                ThemeRankResponse::id,
                ThemeRankResponse::themeName,
                ThemeRankResponse::rank
            )
            .containsExactly(
                tuple(1L, "공포", 1),
                tuple(3L, "잠입", 1),
                tuple(2L, "추리", 3)
            );
    }
}
