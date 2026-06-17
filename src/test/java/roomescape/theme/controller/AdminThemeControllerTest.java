package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.NotFoundException;
import roomescape.theme.application.service.ThemeService;
import roomescape.theme.exception.ThemeErrorMessage;
import roomescape.theme.presentation.controller.AdminThemeController;
import roomescape.theme.presentation.dto.ThemeResponse;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void create_theme() throws Exception {
        given(themeService.save(any()))
                .willReturn(new ThemeResponse(1L, "theme", "description", "img", 30000L));

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "theme",
                                  "description": "description",
                                  "thumbnailImgUrl": "img",
                                  "price": 30000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("theme"));
    }

    @Test
    void create_theme_with_blank_name() throws Exception {
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "description",
                                  "thumbnailImgUrl": "img",
                                  "price": 30000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("[ERROR] 테마 이름은 비어있을 수 없습니다."));
    }

    @Test
    void delete_theme() throws Exception {
        doNothing().when(themeService).delete(1L);

        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_theme_not_found() throws Exception {
        doThrow(new NotFoundException(ThemeErrorMessage.THEME_NOT_FOUND, 1L)).when(themeService).delete(1L);

        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNotFound());
    }
}
