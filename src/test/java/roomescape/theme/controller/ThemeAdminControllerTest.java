package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.WebMvcConfig;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.controller.dto.ThemeRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@WebMvcTest(ThemeAdminController.class)
@Import(WebMvcConfig.class)
class ThemeAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마를 성공적으로 생성한다.")
    void createTheme_Success() throws Exception {
        // given
        ThemeRequest request = new ThemeRequest("테마", "설명", "url");
        Theme theme = new Theme(1L, "테마", "설명", "url");

        given(themeService.save(any())).willReturn(theme);

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("테마"));
    }

    @Test
    @DisplayName("테마 생성 시 필수 필드가 누락되면 400 에러를 반환한다.")
    void createTheme_MissingFields_BadRequest() throws Exception {
        // given
        String requestBody = "{\"name\":\"\", \"description\":\"설명\", \"thumbnailUrl\":\"url\"}";

        // when & then
        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("테마 요청 형식이 유효하지 않습니다."));
    }

    @Test
    @DisplayName("테마를 성공적으로 삭제한다.")
    void deleteTheme_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/themes/1"))
                .andExpect(status().isNoContent());
    }
}
