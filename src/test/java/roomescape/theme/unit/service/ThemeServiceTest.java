package roomescape.theme.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @InjectMocks
    private ThemeService themeService;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        var request = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
        when(themeRepository.existsByName(anyString()))
                .thenReturn(false);
        when(themeRepository.save(any(Theme.class)))
                .thenReturn(new Theme(1L, request.name(), request.description(), request.thumbnail()));

        // when
        var response = themeService.createTheme(request);

        // then
        assertAll(
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.description()).isEqualTo(request.description()),
                () -> assertThat(response.thumbnail()).isEqualTo(request.thumbnail())
        );
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void getAllThemes() {
        // given
        var inDbThemes = List.of(
                new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일"),
                new Theme(2L, "테마2", "테마2 설명", "테마2 썸네일")
        );
        when(themeRepository.findAll())
                .thenReturn(inDbThemes);

        // when
        var responses = themeService.getAllThemes();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.getFirst().name()).isEqualTo("테마1"),
                () -> assertThat(responses.get(1).name()).isEqualTo("테마2")
        );
    }

    @Test
    @DisplayName("인기 테마를 조회한다.")
    void getPopularThemes() {
        // given
        var inDbThemes = List.of(
                new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일"),
                new Theme(2L, "테마2", "테마2 설명", "테마2 썸네일")
        );
        when(themeRepository.findPopularDescendingUpTo(any(LocalDate.class), any(LocalDate.class), anyInt()))
                .thenReturn(inDbThemes);

        // when
        var responses = themeService.getPopularThemes(2);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteTheme() {
        // given
        when(reservationRepository.existsByThemeId(anyLong()))
                .thenReturn(false);

        // when
        themeService.deleteTheme(anyLong());

        // then
        verify(themeRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("이미 예약 된 예약이 있을 경우 삭제할 수없다.")
    void cantDeleteWhenReserved() {
        //given
        when(reservationRepository.existsByThemeId(anyLong()))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> themeService.deleteTheme(anyLong()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("해당 테마에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("중복되는 테마 이름이 있을 경우 생성할 수 없다.")
    void createThemeWithDuplicateName() {
        // given
        var request = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
        when(themeRepository.existsByName(anyString()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }
}
