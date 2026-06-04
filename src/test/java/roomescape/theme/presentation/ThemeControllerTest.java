package roomescape.theme.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminInterceptor;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.domain.Theme;

@WebMvcTest(ThemeController.class)
@Import({AdminInterceptor.class, ThemeControllerTest.TestWebConfig.class})
class ThemeControllerTest {

    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Autowired
        private AdminInterceptor adminInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(adminInterceptor).addPathPatterns("/**");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("테마 저장 요청을 서비스에 전달하고 생성 응답을 반환한다")
    void addNewTheme_success() throws Exception {
        // given
        Theme theme = Theme.createRow(1L, "테마", "테마 설명", "https://image");
        ThemeCreateCommand createCommand = new ThemeCreateCommand("테마", "테마 설명", "https://image");
        given(themeService.save(createCommand)).willReturn(theme);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "테마");
        body.put("description", "테마 설명");
        body.put("thumbnail", "https://image");

        // when & then
        mockMvc.perform(post("/themes")
                        .header("Authorization", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/themes/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("테마"))
                .andExpect(jsonPath("$.description").value("테마 설명"))
                .andExpect(jsonPath("$.thumbnail").value("https://image"));

        then(themeService).should().save(createCommand);
    }

    @Test
    @DisplayName("테마 목록 조회 요청을 서비스에 전달하고 성공 응답을 반환한다")
    void readThemes_success_when_no_params() throws Exception {
        // given
        given(themeService.findAll()).willReturn(
                List.of(Theme.createRow(1L, "기본 테마", "설명", "https://image"))
        );

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("기본 테마"))
                .andExpect(jsonPath("$[0].description").value("설명"))
                .andExpect(jsonPath("$[0].thumbnail").value("https://image"));

        then(themeService).should().findAll();
    }

    @Test
    @DisplayName("기간 기준 인기 테마 조회 요청을 서비스에 전달한다")
    void readThemes_success_when_top_n_by_period() throws Exception {
        // given
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        given(themeService.findTopNByPeriod(from, to, "popular", 2L)).willReturn(
                List.of(
                        Theme.createRow(1L, "테마1", "설명1", "URL1"),
                        Theme.createRow(2L, "테마2", "설명2", "URL2")
                )
        );

        // when & then
        mockMvc.perform(get("/themes")
                        .param("sortBy", "popular")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-31")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("테마1"))
                .andExpect(jsonPath("$[1].name").value("테마2"));

        then(themeService).should().findTopNByPeriod(from, to, "popular", 2L);

    }

    @Test
    @DisplayName("테마 삭제 요청을 서비스에 전달하고 성공 응답을 반환한다")
    void deleteTheme_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/themes/1")
                        .header("Authorization", "ADMIN"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        then(themeService).should().deleteById(1L);
    }
}
