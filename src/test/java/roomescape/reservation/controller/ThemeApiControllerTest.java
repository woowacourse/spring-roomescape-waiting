package roomescape.reservation.controller;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.config.ControllerConfig;
import roomescape.reservation.dto.ThemeCreateRequest;
import roomescape.reservation.dto.ThemeResponse;
import roomescape.reservation.service.ThemeService;

@WebMvcTest(ThemeApiController.class)
@Import(ControllerConfig.class)
class ThemeApiControllerTest {

    @MockBean
    private ThemeService themeService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인기 테마 목록 조회를 성공하면 200 응답을 받는다.")
    void getPopularThemeRequestTest() throws Exception {
        mockMvc.perform(get("/themes/popular")
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("테마 목록 조회에 성공하면 200 응답을 받는다.")
    void getThemeRequestTest() throws Exception {
        mockMvc.perform(get("/themes")
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("테마를 성공적으로 추가하면 201 응답과 Location 헤더에 리소스 저장 경로를 받는다.")
    void createThemeRequestTest() throws Exception {
        ThemeCreateRequest themeCreateRequest = new ThemeCreateRequest("공포", "진짜 무서움", "https://i.pinimg.com/236x.jpg");
        ThemeResponse themeResponse = new ThemeResponse(1L, themeCreateRequest.name(), themeCreateRequest.description(),
                themeCreateRequest.thumbnail());

        doReturn(1L).when(themeService)
                .save(themeCreateRequest);

        doReturn(themeResponse).when(themeService)
                .findById(1L);

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(themeCreateRequest))
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/1"))
                .andExpect(jsonPath("$.id").value(themeResponse.id()));
    }

    @Test
    @DisplayName("테마를 성공적으로 제거하면 204 응답을 받는다.")
    void deleteThemeRequestTest() throws Exception {
        mockMvc.perform(delete("/themes/{id}", 1L)
                        .cookie(new Cookie("token", "value"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
