package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.theme.Theme;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.repository.ThemeQueryDao;
import roomescape.repository.ThemeUpdateDao;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock private ThemeQueryDao themeQueryDao;
    @Mock private ThemeUpdateDao themeUpdateDao;

    @InjectMocks
    private ThemeService themeService;

    @Test
    void 테마_생성_성공() {
        ThemeRequest themeRequest = new ThemeRequest("인형의 집", "공포 테마", "http://example.com");
        when(themeUpdateDao.insert(themeRequest)).thenReturn(1L);

        ThemeResponse themeResponse = themeService.create(themeRequest);

        assertThat(themeResponse.getId()).isNotNull();
        assertThat(themeResponse.getName()).isEqualTo("인형의 집");
    }

    @Test
    void 전체_테마_조회() {
        Theme theme1 = new Theme(1L, "무서운 이야기", "공포", "http://example1.com");
        Theme theme2 = new Theme(2L, "명탐정의 부재", "탐험", "http://example2.com");
        when(themeQueryDao.findAllTheme()).thenReturn(List.of(theme1, theme2));

        List<ThemeResponse> result = themeService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 테마_삭제() {
        themeService.delete(1L);

        verify(themeUpdateDao).delete(1L);
    }

    @Test
    void 예약이_존재하는_테마_삭제시_예외가_발생한다() {
        doThrow(DataIntegrityViolationException.class).when(themeUpdateDao).delete(1L);

        assertThatThrownBy(() -> themeService.delete(1L))
                .isInstanceOf(ReferencedDataException.class);
    }
}