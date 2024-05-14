package roomescape.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import roomescape.service.ThemeService;
import roomescape.web.dto.request.theme.ThemeRequest;
import roomescape.web.dto.response.theme.ThemeResponse;

@WebMvcTest(controllers = ThemeController.class)
class ThemeControllerTest {
    @MockBean
    private ThemeService themeService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예약 테마 저장 시 모든 필드가 유효한 값이면 201Created 상태코드를 반환한다.")
    void saveTheme_ShouldReturn201StatusCode_WhenInsertAllValidField() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("name", "description", "thumbnail");
        Mockito.when(themeService.saveTheme(request))
                .thenReturn(new ThemeResponse(1L, "name", "description", "thumbnail"));

        // when & then
        mockMvc.perform(post("/themes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("예약테마 저장 시 모든 필드가 유효한 값이라면 location 헤더가 추가된다.")
    void saveTheme_ShouldRedirect_WhenInsertAllValidateField() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("name", "description", "thumbnail");
        Mockito.when(themeService.saveTheme(request))
                .thenReturn(new ThemeResponse(1L, "name", "description", "thumbnail"));

        // when & then
        mockMvc.perform(post("/themes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(header().string(LOCATION, "/themes/1"));
    }

    @Test
    @DisplayName("예약테마 저장 시 이름이 빈값이면 400 BadRequest를 반환한다.")
    void saveTheme_ShouldReturn400StatusCode_WhenInsertBlankName() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("  ", "description", "thumbnail");
        Mockito.when(themeService.saveTheme(request))
                .thenReturn(new ThemeResponse(1L, "", "description", "thumbnail"));

        // when & then
        mockMvc.perform(post("/themes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("이름은 빈값을 허용하지 않습니다.")));
    }

    @Test
    @DisplayName("예약테마 저장 시 설명이 빈값이면 400 BadRequest를 반환한다.")
    void saveTheme_ShouldReturn400StatusCode_WhenInsertBlankDescription() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("name", " ", "thumbnail");
        Mockito.when(themeService.saveTheme(request))
                .thenReturn(new ThemeResponse(1L, "name", " ", "thumbnail"));

        // when & then
        mockMvc.perform(post("/themes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("설명은 빈값을 허용하지 않습니다.")));
    }


    @Test
    @DisplayName("예약테마 저장 시 썸네일이 빈값이면 400 BadRequest를 반환한다.")
    void saveTheme_ShouldReturn400StatusCode_WhenInsertBlankThumbnail() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("name", "description", " ");
        Mockito.when(themeService.saveTheme(request))
                .thenReturn(new ThemeResponse(1L, "name", "description", ""));

        // when & then
        mockMvc.perform(post("/themes")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("썸내일은 빈값을 허용하자 않습니다.")));
    }
}
