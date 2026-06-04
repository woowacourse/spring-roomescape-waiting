package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.AlreadyInUseException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

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

        ThemeResponse created = themeService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("새 테마");
    }

    @Test
    void 예약_없는_테마_삭제() {
        themeService.delete(11L);
    }

    @Test
    void 예약_존재하는_테마_삭제_시_예외() {
        Long themeIdWithReservation = 1L;

        assertThatThrownBy(() -> themeService.delete(themeIdWithReservation))
                .isInstanceOf(AlreadyInUseException.class);
    }
}
