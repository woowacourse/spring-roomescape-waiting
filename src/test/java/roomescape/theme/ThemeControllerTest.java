package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.theme.application.ThemeService;
import roomescape.theme.dto.request.ThemeSaveRequest;
import roomescape.theme.dto.response.ThemeFindResponse;
import roomescape.theme.dto.response.ThemeSaveResponse;
import roomescape.theme.presentation.ManagerThemeController;
import roomescape.theme.presentation.UserThemeController;

@ExtendWith(MockitoExtension.class)
class ThemeControllerTest {

    @Mock
    private ThemeService themeService;

    private UserThemeController userThemeController;
    private ManagerThemeController managerThemeController;

    @BeforeEach
    void setUp() {
        userThemeController = new UserThemeController(themeService);
        managerThemeController = new ManagerThemeController(themeService);
    }

    @Test
    void 날짜별_테마_목록_조회_응답_테스트() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        List<ThemeFindResponse> serviceResponse = List.of(themeFindResponse());
        when(themeService.findThemesBySlotDate(date)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<ThemeFindResponse>>> response =
                userThemeController.findThemesBySlotDate(date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 테마_생성_응답_테스트() {
        ThemeSaveRequest request = new ThemeSaveRequest("theme", "description", "thumbnail");
        ThemeSaveResponse serviceResponse = new ThemeSaveResponse(1L, "theme", "description", "thumbnail");
        when(themeService.save(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ThemeSaveResponse>> response = managerThemeController.save(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    void 테마_삭제_응답_테스트() {
        ResponseEntity<ApiResponse<Void>> response = managerThemeController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(themeService).delete(1L);
    }

    private ThemeFindResponse themeFindResponse() {
        return new ThemeFindResponse(1L, "theme", "description", "thumbnail");
    }
}
