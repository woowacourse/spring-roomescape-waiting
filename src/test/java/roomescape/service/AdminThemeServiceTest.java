package roomescape.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.dto.ThemeRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminThemeServiceTest {

    @Autowired
    private AdminThemeService adminThemeService;

    @DisplayName("이미 사용중인 테마는 삭제하면 에러를 던진다.")
    @Test
    void 테마_삭제_예외_테스트() {
        long themeId = 1L;
        assertThatThrownBy(() -> adminThemeService.delete(themeId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNALLOWED_DELETE_EXISTS_THEME.getMessage());
    }

    @DisplayName("존재하는 테마를 추가하면 에러를 던진다.")
    @Test
    void 테마_추가_예외_테스트() {
        ThemeRequest themeRequest = new ThemeRequest("공포의 저택", "무서워요", "url", 10000L);
        assertThatThrownBy(() -> adminThemeService.save(themeRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTS_THEME.getMessage());
    }
}
