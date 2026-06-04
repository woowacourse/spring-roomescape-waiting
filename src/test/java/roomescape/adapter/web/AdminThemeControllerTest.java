package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.ThemeService;
import roomescape.application.dto.result.ThemeResult;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * AdminThemeController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>고유 책임: 테마 생성 요청 본문의 @Valid 검증(@NotBlank)과, 서비스 예외의 상태코드 변환.
 * 서비스는 @MockitoBean으로 대체한다.
 */
@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Nested
    @DisplayName("테마 생성 POST /admin/themes")
    class Create {

        @Test
        @DisplayName("정상 요청이면 201")
        void 정상_생성() throws Exception {
            given(themeService.create(any()))
                    .willReturn(new ThemeResult(1L, "테마A", "설명", "url"));

            mockMvc.perform(post("/admin/themes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"테마A","description":"설명","thumbnailUrl":"url"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("테마A"));
        }

        @Test
        @DisplayName("[입력 게이트] 이름이 비어 있으면 400")
        void 빈_이름_400() throws Exception {
            mockMvc.perform(post("/admin/themes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"","description":"설명","thumbnailUrl":"url"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이름은 비어 있을 수 없습니다."));
        }

        @Test
        @DisplayName("[입력 게이트] 설명이 비어 있으면 400")
        void 빈_설명_400() throws Exception {
            mockMvc.perform(post("/admin/themes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"테마A","description":"","thumbnailUrl":"url"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("설명은 비어 있을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("테마 삭제 DELETE /admin/themes/{id}")
    class Delete {

        @Test
        @DisplayName("정상 삭제면 204")
        void 정상_삭제() throws Exception {
            mockMvc.perform(delete("/admin/themes/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[예외 변환] 예약이 존재하는 테마 삭제 시도 → 400 + 메시지")
        void 사용중_테마_400() throws Exception {
            doThrow(new BusinessRuleViolationException("예약이 존재하는 테마는 삭제할 수 없습니다."))
                    .when(themeService).delete(any());

            mockMvc.perform(delete("/admin/themes/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("예약이 존재하는 테마는 삭제할 수 없습니다."));
        }
    }
}
