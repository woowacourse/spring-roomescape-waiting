package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationName;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.theme.ThemeCreateRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.fixture.FakeThemeRepositoryFixture;
import roomescape.repository.FakeThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeServiceTest {

    private final FakeThemeRepository themeRepository = FakeThemeRepositoryFixture.create();
    private final ThemeService themeService = new ThemeService(themeRepository);

    @Nested
    @DisplayName("테마 생성")
    class CreateTheme {

        @DisplayName("요청에 따라 Theme을 생성 할 수 있다")
        @Test
        void createThemeTest() {
            // given
            ThemeCreateRequest requestDto = new ThemeCreateRequest("테마 가이온", "가이온이 코딩을 합니다", "https://가이온_코딩중.png");

            // when
            ThemeResponse responseDto = themeService.createTheme(requestDto);

            // then
            assertAll(
                    () -> assertThat(responseDto.id()).isEqualTo(3L),
                    () -> assertThat(responseDto.name()).isEqualTo("테마 가이온"),
                    () -> assertThat(responseDto.description()).isEqualTo("가이온이 코딩을 합니다"),
                    () -> assertThat(responseDto.thumbnail()).isEqualTo("https://가이온_코딩중.png")
            );
        }

        @DisplayName("이미 테마가 존재하면 Theme을 생성할 수 없다")
        @Test
        void createInvalidThemeTest() {
            // given
            ThemeCreateRequest requestDto = new ThemeCreateRequest("테마 가이온", "가이온이 코딩을 합니다", "https://가이온_코딩중.png");

            // when
            themeService.createTheme(requestDto);

            // then
            assertThatThrownBy(() -> themeService.createTheme(requestDto)).isInstanceOf(DuplicateContentException.class);
        }
    }

    @Nested
    @DisplayName("테마 조회")
    class FindTheme {

        @DisplayName("모든 Theme을 조회할 수 있다")
        @Test
        void findAllThemesTest() {
            // when
            List<ThemeResponse> responses = themeService.findAllThemes();

            // then
            assertThat(responses).hasSize(2);
        }

        @DisplayName("지난 7일 간 예약된 테마들 중 예약이 많이 된 순으로 조회할 수 있다")
        @Test
        void findPopularThemesTest() {
            // given
            ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
            Theme theme1 = themeRepository.findById(1L).get();
            Theme theme2 = themeRepository.findById(2L).get();

            // when
            themeRepository.addReservation(new Reservation(1L, new ReservationName(1L, "a"), LocalDate.now().minusDays(1), reservationTime, theme1));
            themeRepository.addReservation(new Reservation(2L, new ReservationName(1L, "a"), LocalDate.now().minusDays(2), reservationTime, theme2));
            themeRepository.addReservation(new Reservation(3L, new ReservationName(1L, "a"), LocalDate.now().minusDays(3), reservationTime, theme2));
            themeRepository.addReservation(new Reservation(4L, new ReservationName(1L, "a"), LocalDate.now().minusDays(10), reservationTime, theme1));
            List<ThemeResponse> responses = themeService.findPopularThemes();

            // then
            assertAll(
                    () -> assertThat(responses).hasSize(2),
                    () -> assertThat(responses.get(0)).isEqualTo(ThemeResponse.from(theme2)),
                    () -> assertThat(responses.get(1)).isEqualTo(ThemeResponse.from(theme1))
            );
        }
    }

    @Nested
    @DisplayName("테마 삭제")
    class DeleteTheme {

        @DisplayName("Theme을 삭제할 수 있다")
        @Test
        void deleteThemeByIdTest() {
            // when
            themeService.deleteThemeById(1L);

            // then
            List<ThemeResponse> allTimes = themeService.findAllThemes();
            assertThat(allTimes.size()).isEqualTo(1);
        }

        @DisplayName("존재하지 않는 Theme을 삭제하려고 하면 예외가 발생한다")
        @Test
        void deleteNonExistentThemeTest() {
            // given
            Long missingId = 20L;

            // when & then
            assertThatThrownBy(() -> themeService.deleteThemeById(missingId)).isInstanceOf(NotFoundException.class);
        }
    }
}
