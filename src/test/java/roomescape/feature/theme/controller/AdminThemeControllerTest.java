package roomescape.feature.theme.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.feature.theme.domain.ThemeDescription;
import roomescape.feature.theme.domain.ThemeImageUrl;
import roomescape.feature.theme.domain.ThemeName;
import roomescape.feature.theme.dto.command.ThemeCreateCommand;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.theme.error.type.ThemeErrorType;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.service.ThemeService;
import roomescape.fixture.ThemeFixture;
import roomescape.global.error.exception.GeneralException;
import roomescape.support.WebMvcControllerTest;

@WebMvcControllerTest(controllers = AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ThemeMapper themeMapper;

    private static final String VALID_NAME = "\"%s\"".formatted(ThemeFixture.VALID.getName());
    private static final String VALID_DESCRIPTION = "\"%s\"".formatted(ThemeFixture.VALID.getDescription());
    private static final String VALID_IMAGE_URL = "\"%s\"".formatted(ThemeFixture.VALID.getImageUrl());
    private static final String OMITTED = "null";

    private String themeRequestBody(String name, String description, String imageUrl) {
        return """
            {
              "name": %s,
              "description": %s,
              "imageUrl": %s
            }
            """.formatted(name, description, imageUrl);
    }

    @Nested
    class 테마_목록_조회 {

        @Test
        void 삭제된_테마를_포함한_전체_테마_목록을_조회한다() throws Exception {
            when(themeService.getAllThemes()).thenReturn(List.of(
                new ThemeResponseDto(1L, "테마1", "설명1", "https://example.com/1.png", false),
                new ThemeResponseDto(2L, "테마2", "설명2", "https://example.com/2.png", true)
            ));

            mockMvc.perform(get("/api/admin/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].deleted", equalTo(true)));
        }
    }

    @Nested
    class 테마_생성 {

        @Test
        void 테마를_생성한다() throws Exception {
            when(themeMapper.toCreateCommand(any())).thenReturn(new ThemeCreateCommand(
                new ThemeName(ThemeFixture.VALID.getName()),
                new ThemeDescription(ThemeFixture.VALID.getDescription()),
                new ThemeImageUrl(ThemeFixture.VALID.getImageUrl())
            ));
            when(themeService.saveTheme(any())).thenReturn(new ThemeResponseDto(1L,
                ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(),
                ThemeFixture.VALID.getImageUrl(),
                false
            ));

            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "description": "%s",
                          "imageUrl": "%s"
                        }
                        """.formatted(
                            ThemeFixture.VALID.getName(),
                            ThemeFixture.VALID.getDescription(),
                            ThemeFixture.VALID.getImageUrl()
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo(ThemeFixture.VALID.getName())))
                .andExpect(jsonPath("$.description", equalTo(ThemeFixture.VALID.getDescription())));
        }

        @Test
        void 테마_이름이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(themeRequestBody(OMITTED, VALID_DESCRIPTION, VALID_IMAGE_URL)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 테마_설명이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(themeRequestBody(VALID_NAME, OMITTED, VALID_IMAGE_URL)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 테마_이미지_URL이_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(themeRequestBody(VALID_NAME, VALID_DESCRIPTION, OMITTED)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 올바르지_않은_이미지_URL이면_4xx를_반환한다() throws Exception {
            when(themeMapper.toCreateCommand(any())).thenThrow(new GeneralException(ThemeErrorType.INVALID_IMAGE_URL));

            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "description": "%s",
                          "imageUrl": "%s"
                        }
                        """.formatted(
                            ThemeFixture.VALID.getName(),
                            ThemeFixture.VALID.getDescription(),
                            ThemeFixture.INVALID_URL_FORMAT.getImageUrl()
                        )))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 이미_등록된_테마_이름이면_4xx를_반환한다() throws Exception {
            when(themeMapper.toCreateCommand(any())).thenReturn(new ThemeCreateCommand(
                new ThemeName(ThemeFixture.VALID.getName()),
                new ThemeDescription(ThemeFixture.VALID.getDescription()),
                new ThemeImageUrl(ThemeFixture.VALID.getImageUrl())
            ));
            when(themeService.saveTheme(any())).thenThrow(new GeneralException(ThemeErrorType.ALREADY_EXIST_THEME));

            mockMvc.perform(post("/api/admin/themes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "%s",
                          "description": "%s",
                          "imageUrl": "%s"
                        }
                        """.formatted(
                            ThemeFixture.VALID.getName(),
                            ThemeFixture.VALID.getDescription(),
                            ThemeFixture.VALID.getImageUrl()
                        )))
                .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    class 테마_삭제 {

        @Test
        void 테마를_삭제한다() throws Exception {
            doNothing().when(themeService).deleteThemeById(1L);

            mockMvc.perform(delete("/api/admin/themes/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        void 존재하지_않는_테마_ID이면_4xx를_반환한다() throws Exception {
            doThrow(new GeneralException(ThemeErrorType.THEME_NOT_FOUND)).when(themeService).deleteThemeById(999L);

            mockMvc.perform(delete("/api/admin/themes/999"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 음수_ID이면_4xx를_반환한다() throws Exception {
            mockMvc.perform(delete("/api/admin/themes/-1"))
                .andExpect(status().is4xxClientError());
        }
    }
}
