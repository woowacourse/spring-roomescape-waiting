package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.api.ApiResponse;
import roomescape.theme.adapter.in.web.ManagerThemeController;
import roomescape.theme.adapter.in.web.UserThemeController;
import roomescape.theme.application.dto.request.ThemeSaveRequest;
import roomescape.theme.application.dto.response.ThemeFindResponse;
import roomescape.theme.application.dto.response.ThemeSaveResponse;
import roomescape.theme.application.port.in.CreateThemeUseCase;
import roomescape.theme.application.port.in.DeleteThemeUseCase;
import roomescape.theme.application.port.in.FindThemeUseCase;

@ExtendWith(MockitoExtension.class)
class ThemeControllerTest {

    @Mock
    private CreateThemeUseCase createThemeUseCase;
    @Mock
    private FindThemeUseCase findThemeUseCase;
    @Mock
    private DeleteThemeUseCase deleteThemeUseCase;

    private UserThemeController userThemeController;
    private ManagerThemeController managerThemeController;

    @BeforeEach
    void setUp() {
        userThemeController = new UserThemeController(findThemeUseCase);
        managerThemeController = new ManagerThemeController(createThemeUseCase, findThemeUseCase, deleteThemeUseCase);
    }

    @Test
    @DisplayName("날짜별 테마 목록 조회 응답을 반환한다.")
    void returns_themes_by_date_response() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        List<ThemeFindResponse> serviceResponse = List.of(themeFindResponse());
        when(findThemeUseCase.findThemesBySlotDate(date)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<List<ThemeFindResponse>>> response =
                userThemeController.findThemesBySlotDate(date);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    private ThemeFindResponse themeFindResponse() {
        return new ThemeFindResponse(1L, "theme", "description", "thumbnail");
    }

    @Test
    @DisplayName("테마 생성 응답을 반환한다.")
    void returns_theme_create_response() {
        ThemeSaveRequest request = new ThemeSaveRequest("theme", "description", "thumbnail");
        ThemeSaveResponse serviceResponse = new ThemeSaveResponse(1L, "theme", "description", "thumbnail");
        when(createThemeUseCase.save(request)).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse<ThemeSaveResponse>> response = managerThemeController.save(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(serviceResponse);
    }

    @Test
    @DisplayName("테마 삭제 응답을 반환한다.")
    void returns_theme_delete_response() {
        ResponseEntity<ApiResponse<Void>> response = managerThemeController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(deleteThemeUseCase).delete(1L);
    }

}
