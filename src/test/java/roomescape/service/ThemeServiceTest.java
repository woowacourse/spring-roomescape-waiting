package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    void 전체_테마_조회() {
        List<ThemeResponse> result = themeService.findAllThemes();

        assertThat(result).hasSize(15);
    }

    @Test
    void 인기_테마_상위_3개_조회() {
        List<ThemeResponse> result = themeService.findTopTheme(3L);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("우테코 공포물");
        assertThat(result.get(1).name()).isEqualTo("미래 도시");
        assertThat(result.get(2).name()).isEqualTo("고대 이집트");
    }

    @Test
    void 테마_생성() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes()
        );
        ThemeRequest request = new ThemeRequest("새 테마", "새 테마 설명", file);

        themeService.create(request);

        assertThat(themeService.findAllThemes()).hasSize(16);
    }

    @Test
    void 예약_없는_테마_삭제() {
        themeService.delete(11L);

        assertThat(themeService.findAllThemes()).hasSize(14);
    }
}
