package roomescape.theme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import roomescape.theme.controller.dto.ThemeCreateRequest;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("테마를 생성하는 요청을 하면 생성된 테마 정보가 응답으로 반환된다.")
    void create_success_response() throws Exception {
        // given
        Theme theme = Theme.of(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme-1.png");

        given(themeService.create(anyString(), anyString(), anyString()))
                .willReturn(theme);

        ThemeCreateRequest request = new ThemeCreateRequest("brown", "설명", "섬네일");

        // when then
        MvcResult result = mockMvc.perform(
                        post("/admin/themes")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        ThemeResponse themeResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ThemeResponse.class
        );

        assertThat(themeResponse).extracting(
                ThemeResponse::id,
                ThemeResponse::name,
                ThemeResponse::description,
                ThemeResponse::thumbnail
        ).containsExactly(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());

        then(themeService).should().create(request.name(), request.description(), request.thumbnail());
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",설명,섬네일",
            "이름,,섬네일",
            "이름,설명,",
    })
    @DisplayName("테마를 생성하는 요청을 할 때 특정 요청값이 비어있으면 에러가 발생한다.")
    void create_fail_emptyField(String name, String description, String thumbnail) throws Exception {
        // given
        ThemeCreateRequest request = new ThemeCreateRequest(name, description, thumbnail);

        // when then
        mockMvc.perform(
                        post("/admin/themes")
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("특정 테마를 삭제하는 요청을 한다.")
    void delete_success_noContent() throws Exception {
        // when then
        long id = 1L;
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/admin/themes/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(themeService).should().delete(id);
    }

}
