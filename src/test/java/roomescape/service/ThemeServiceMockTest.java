package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.ThemeResponse;

@ExtendWith(MockitoExtension.class)
class ThemeServiceMockTest {

    @InjectMocks
    private ThemeService themeService;
    @Mock
    private ThemeRepository themeRepository;

    @DisplayName("테마를 저장한다.")
    @Test
    void createTheme() {
        // given
        Theme savedTheme = new Theme(1L, "happy", "hi", "abcd.html");
        when(themeRepository.save(any())).thenReturn(savedTheme);

        // when && then
        assertThat(themeService.createTheme(new ThemeRequest("happy", "hi", "abcd.html")))
                .isEqualTo(ThemeResponse.from(savedTheme));
    }

    @DisplayName("모든 테마 조회한다.")
    @Test
    void findAllThemes() {
        //given
        List<Theme> themes = List.of(
                new Theme(1L, "happy", "hi", "abcd.html"),
                new Theme(2L, "happy2", "hi2", "abcd.html2")
        );
        when(themeRepository.findAll()).thenReturn(themes);

        // when && then
        assertThat(themeService.findAllThemes())
                .hasSize(2);
    }



}
