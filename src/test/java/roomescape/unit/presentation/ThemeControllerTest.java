package roomescape.unit.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.AuthorizationExtractor;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.presentation.ThemeController;
import roomescape.service.ThemeService;

@WebMvcTest(value = {ThemeController.class})
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private AuthorizationExtractor authorizationExtractor;

    @Test
    void 테마를_전체_조회한다() throws Exception {
        // given
        ThemeResponse theme = new ThemeResponse(1L, "theme1", "desc", "thumb1");
        List<ThemeResponse> response = List.of(theme);
        given(themeService.findAllThemes()).willReturn(response);
        // when & then
        mockMvc.perform(get("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    void 테마를_추가하는데_성공한다() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("theme1", "desc", "thumb1");
        ThemeResponse response = new ThemeResponse(1L, "theme1", "desc", "thumb1");
        given(themeService.createTheme(request)).willReturn(response);
        // when & then
        mockMvc.perform(post("/api/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/themes/" + 1))
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }
}