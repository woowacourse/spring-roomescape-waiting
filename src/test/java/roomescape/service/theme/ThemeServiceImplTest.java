package roomescape.service.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.repository.theme.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {
    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private ThemeServiceImpl themeService;

    @DisplayName("존재하지 않는 테마 삭제를 시도하면 예외를 발생시킨다")
    @Test
    void delete() {
        // given
        long id = 99L;

        // when
        when(themeRepository.deleteById(id)).thenReturn(0);

        // then
        assertThatThrownBy(() -> themeService.deleteById(id))
                .isInstanceOf(ThemeNotFoundException.class);
    }
}
