package roomescape.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeServiceTest {

    ThemeService themeService;

    @Nested
    @DisplayName("테마 생성")
    class CreateTheme {

        private final ReservationRepository reservationRepository = new FakeReservationRepository(new ArrayList<>());
        private final ThemeRepository themeRepository = new FakeThemeRepository(new ArrayList<>());

        @DisplayName("요청에 따라 Theme을 생성 할 수 있다")
        @Test
        void createThemeTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            ThemeCreateRequestDto requestDto = new ThemeCreateRequestDto("테마 가이온", "가이온이 코딩을 합니다", "가이온_코딩중.png");
            ThemeResponseDto responseDto = themeService.createTheme(requestDto);

            Long id = responseDto.id();
            String name = requestDto.name();
            String description = requestDto.description();
            String thumbnail = requestDto.thumbnail();

            Assertions.assertAll(
                    () -> assertThat(id).isEqualTo(1L),
                    () -> assertThat(name).isEqualTo("테마 가이온"),
                    () -> assertThat(description).isEqualTo("가이온이 코딩을 합니다"),
                    () -> assertThat(thumbnail).isEqualTo("가이온_코딩중.png")
            );
        }

        @DisplayName("이미 같은 이름의 테마가 존재하면 Theme을 생성할 수 없다")
        @Test
        void createDuplicatedNameThemeTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            ThemeCreateRequestDto requestDto = new ThemeCreateRequestDto("테마 가이온", "가이온이 코딩을 합니다", "가이온_코딩중.png");
            ThemeCreateRequestDto invalidRequestDto = new ThemeCreateRequestDto("테마 가이온", "가이온이 코딩을 합니다", "가이온_코딩중.png");
            themeService.createTheme(requestDto);

            assertThatThrownBy(() -> themeService.createTheme(invalidRequestDto)).isInstanceOf(DuplicateContentException.class);
        }

        @DisplayName("같은 이름의 테마가 존재하지 않으면 Theme을 생성할 수 있다.")
        @Test
        void createValidNameThemeTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            ThemeCreateRequestDto requestDto = new ThemeCreateRequestDto("테마 가이온1", "가이온이 코딩을 합니다", "가이온_코딩중.png");
            ThemeCreateRequestDto invalidRequestDto = new ThemeCreateRequestDto("테마 가이온", "가이온이 코딩을 합니다", "가이온_코딩중.png");
            themeService.createTheme(requestDto);

            Assertions.assertDoesNotThrow(() -> themeService.createTheme(invalidRequestDto));
        }
    }

    @Nested
    @DisplayName("테마 조회")
    class FindTheme {

        private final FakeThemeRepository themeRepository = new FakeThemeRepository(new ArrayList<>(List.of(
                new Theme(1L, "a", "a", "a"),
                new Theme(2L, "b", "b", "b"),
                new Theme(3L, "c", "c", "c")
        )));
        private final ReservationRepository reservationRepository = new FakeReservationRepository(new ArrayList<>());

        @DisplayName("모든 Theme을 조회할 수 있다")
        @Test
        void findAllThemesTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            List<ThemeResponseDto> responses = themeService.findAllThemes();

            assertThat(responses).hasSize(3);
        }

        @DisplayName("지난 7일 간 예약된 테마들 중 인기 테마를 최대 10건 조회할 수 있다")
        @Test
        void findPopularThemesTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);
            Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");

            ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
            Theme theme1 = themeRepository.findById(1L).get();
            Theme theme2 = themeRepository.findById(2L).get();
            themeRepository.addReservation(new Reservation(1L, member, LocalDate.now().minusDays(2), reservationTime, theme1));
            themeRepository.addReservation(new Reservation(2L, member, LocalDate.now().minusDays(3), reservationTime, theme2));
            themeRepository.addReservation(new Reservation(3L, member, LocalDate.now().minusDays(4), reservationTime, theme2));

            List<ThemeResponseDto> responses = themeService.findPopularThemes();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0)).isEqualTo(ThemeResponseDto.from(theme2));
        }
    }

    @Nested
    @DisplayName("테마 삭제")
    class DeleteTheme {

        private final ReservationRepository reservationRepository = new FakeReservationRepository(new ArrayList<>());
        private final ThemeRepository themeRepository = new FakeThemeRepository(new ArrayList<>());

        @DisplayName("Theme을 삭제할 수 있다")
        @Test
        void deleteThemeByIdTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            ThemeCreateRequestDto requestDto = new ThemeCreateRequestDto("a", "a", "a");
            ThemeResponseDto responseDto = themeService.createTheme(requestDto);

            themeService.deleteThemeById(responseDto.id());

            List<ThemeResponseDto> allTimes = themeService.findAllThemes();
            assertThat(allTimes).isEmpty();
        }

        @DisplayName("존재하지 않는 Theme을 삭제하려고 하면 예외가 발생한다")
        @Test
        void deleteNonExistentThemeTest() {
            themeService = new ThemeService(reservationRepository, themeRepository);

            Long nonExistentId = 2L;

            AssertionsForClassTypes.assertThatThrownBy(() -> themeService.deleteThemeById(nonExistentId))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
