package roomescape.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.service.ThemeService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 테마_목록을_조회한다() throws Exception {
        // given
        given(themeService.findAll())
                .willReturn(List.of(theme()));

        // when & then
        mockMvc.perform(get("/admin/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테마"))
                .andExpect(jsonPath("$[0].description").value("설명"))
                .andExpect(jsonPath("$[0].thumbnail").value("썸네일"));

        verify(themeService, times(1)).findAll();
        verifyNoMoreInteractions(themeService);
    }

    @Test
    void 테마를_생성한다() throws Exception {
        // given
        given(themeService.create("테마", "설명", "썸네일"))
                .willReturn(theme());

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "테마",
                                  "description": "설명",
                                  "thumbnail": "썸네일"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/admin/themes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("테마"));

        verify(themeService, times(1)).create("테마", "설명", "썸네일");
        verifyNoMoreInteractions(themeService);
    }

    @Test
    void 테마_생성_요청값이_유효하지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "설명",
                                  "thumbnail": "썸네일"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(themeService);
    }

    @Test
    void 테마를_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNoContent());

        verify(themeService, times(1)).delete(1L);
        verifyNoMoreInteractions(themeService);
    }

    @Test
    void 삭제_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/themes/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(themeService);
    }

    @Test
    void 예약이_존재하는_테마는_삭제시_에러_응답() throws Exception {
        // given
        doThrow(new RoomescapeException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 테마는 삭제할 수 없습니다."))
                .when(themeService)
                .delete(1L);

        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_IN_USE"))
                .andExpect(jsonPath("$.detail").value("예약이 존재하는 테마는 삭제할 수 없습니다."));

        verify(themeService, times(1)).delete(1L);
        verifyNoMoreInteractions(themeService);
    }

    private Theme theme() {
        return new Theme(1L, "테마", "설명", "썸네일");
    }
}
