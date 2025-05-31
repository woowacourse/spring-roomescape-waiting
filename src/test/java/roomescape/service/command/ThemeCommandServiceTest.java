package roomescape.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import roomescape.domain.reservation.slot.Theme;
import roomescape.dto.theme.ThemeCreateRequestDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.repository.JpaThemeRepository;

public class ThemeCommandServiceTest {

    @Mock
    private JpaThemeRepository themeRepository;

    @InjectMocks
    private ThemeCommandService themeCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTheme() {
        //given
        ThemeCreateRequestDto requestDto = new ThemeCreateRequestDto("공포 테마", "무서운 배경 설명", "image-url");
        Theme theme = requestDto.createWithoutId();
        Theme savedTheme = new Theme(1L, theme.getName(), theme.getDescription(), theme.getThumbnail());

        when(themeRepository.save(any(Theme.class))).thenReturn(savedTheme);

        //when
        ThemeResponseDto response = themeCommandService.createTheme(requestDto);

        //then
        assertEquals(savedTheme.getId(), response.id());
        assertEquals(savedTheme.getName(), response.name());
        assertEquals(savedTheme.getDescription(), response.description());
    }
}
