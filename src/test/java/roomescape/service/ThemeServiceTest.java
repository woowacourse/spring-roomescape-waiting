package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import roomescape.config.UploadProperties;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeDao;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeService = new ThemeService(themeDao, new UploadProperties(System.getProperty("java.io.tmpdir")));
    }

    @Test
    void 전체_테마_조회() {
        given(themeDao.findAllThemes()).willReturn(List.of(
                new Theme(1L, "테마A", "설명", "/a"),
                new Theme(2L, "테마B", "설명", "/b")));

        List<ThemeResponse> result = themeService.findAllThemes();

        assertThat(result).extracting(ThemeResponse::name).containsExactly("테마A", "테마B");
    }

    @Test
    void 인기_테마_조회() {
        given(themeDao.findTopThemes(3L)).willReturn(List.of(
                new Theme(1L, "인기", "설명", "/popular"),
                new Theme(2L, "보통", "설명", "/normal")));

        List<ThemeResponse> result = themeService.findTopTheme(3L);

        assertThat(result).extracting(ThemeResponse::name).containsExactly("인기", "보통");
    }

    @Test
    void 테마_생성() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes());
        ThemeRequest request = new ThemeRequest("새 테마", "새 테마 설명", file);
        given(themeDao.save(any())).willReturn(new Theme(1L, "새 테마", "새 테마 설명", "/images/x.jpg"));

        ThemeResponse result = themeService.create(request);

        assertThat(result.name()).isEqualTo("새 테마");
        verify(themeDao).save(any());
    }

    @Test
    void 테마_삭제() {
        themeService.delete(1L);

        verify(themeDao).delete(1L);
    }
}
