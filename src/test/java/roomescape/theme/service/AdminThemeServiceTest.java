package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.dto.AdminThemeRequest;
import roomescape.theme.dto.AdminThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeServiceTest {

    @Autowired
    private AdminThemeService adminThemeService;

    private AdminThemeRequest 테마요청() {
        return new AdminThemeRequest("테마5", "설명", "https://image.com");
    }

    @Test
    @DisplayName("테마 생성 성공")
    void 테마_생성_성공() {
        AdminThemeResponse response = adminThemeService.createTheme(테마요청());
        assertThat(response.id()).isNotNull();
    }

    @Test
    @DisplayName("전체 테마 조회")
    void 전체_테마_조회() {
        List<AdminThemeResponse> themes = adminThemeService.getAllThemes();
        assertThat(themes).hasSize(4);
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void 테마_삭제_성공() {
        AdminThemeResponse created = adminThemeService.createTheme(테마요청());
        adminThemeService.deleteTheme(created.id());

        assertThat(adminThemeService.getAllThemes()).hasSize(4);
    }

    @Test
    @DisplayName("예약이 존재하는 테마는 삭제할 수 없다")
    void 예약_있는_테마_삭제_불가() {
        assertThatThrownBy(() -> adminThemeService.deleteTheme(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_HAS_RESERVATION))
                .hasMessage(ErrorCode.THEME_HAS_RESERVATION.getMessage());
    }
}
