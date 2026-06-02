package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@WebMvcTest(ThemeAdminController.class)
class ThemeAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ThemeService themeService;

    @DisplayName("ThemeRequest를 받아 새로운 테마를 생성하고, 201을 반환한다.")
    @Test
    void createTheme_success() throws Exception {
        //given
        when( themeService.registerTheme(any()))
                .thenReturn(new Theme(1L, "테마", "설명", "url"));

        String body = """
                {
                    "name": "테마",
                    "description": "설명",
                    "thumbnailUrl": "url"
                }
                """;

        //when & then
        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        ).andExpect(status().isCreated());
    }

    @DisplayName("테마 등록 시, 필드가 하나라도 생략되면 400을 반환한다.")
    @Test
    void createTheme_no_field() throws Exception {
        //given
        when( themeService.registerTheme(any()))
                .thenReturn(new Theme(1L, "테마", "설명", "url"));

        String noName = """
                {
                    "description": "설명",
                    "thumbnailUrl": "url"
                }
                """;

        String noDescription = """
                {
                    "name": "테마",
                    "thumbnailUrl": "url"
                }
                """;

        String noThumbnailUrl = """
                {
                    "name": "테마",
                    "description": "설명"
                }
                """;

        //when & then
        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noName)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noDescription)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(noThumbnailUrl)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("테마 생성 시, 필드가 하나라도 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void createTheme_invalid_field() throws Exception {
        //given
        when( themeService.registerTheme(any()))
                .thenReturn(new Theme(1L, "테마", "설명", "url"));

        String invalidName = """
                {
                    "name": "",
                    "description": "설명",
                    "thumbnailUrl": "url"
                }
                """;

        String invalidDescription = """
                {
                    "name": "테마",
                    "description": "",
                    "thumbnailUrl": "url"
                }
                """;

        String invalidThumbnailUrl = """
                {
                    "name": "테마",
                    "description": "설명",
                    "thumbnailUrl": ""
                }
                """;

        //when & then
        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidName)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDescription)
        ).andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidThumbnailUrl)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("id를 받아 테마를 삭제하고 200을 반환한다.")
    @Test
    void deleteTime_success() throws Exception {
        mockMvc.perform(
                delete("/admin/themes/{id}", 1)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("테마 삭제 시, id 형식이 유효하지 않으면 400을 반환한다.")
    @Test
    void deleteTime_fail() throws Exception {
        mockMvc.perform(
                delete("/admin/themes/{id}", "invalid")
        ).andExpect(status().isBadRequest());
    }
}
