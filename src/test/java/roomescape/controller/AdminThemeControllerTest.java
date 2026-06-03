package roomescape.controller;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Theme;
import roomescape.dto.theme.command.CreateThemeCommand;
import roomescape.infrastructure.AuthInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ThemeService;

@WebMvcTest(controllers = AdminThemeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, AuthInterceptor.class, LoginUserArgumentResolver.class}))
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    @DisplayName("POST /admin/themes - 생성된 id를 Location 헤더에 담아 201을 반환한다")
    void createThemeReturns201WithLocationHeader() throws Exception {
        given(themeService.createTheme(any(CreateThemeCommand.class)))
                .willReturn(new Theme(7L, "공포", "무서움", "https://thumbnail.url"));

        Map<String, Object> body = Map.of(
                "name", "공포",
                "description", "무서움",
                "thumbnailImageUrl", "https://thumbnail.url");

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/7"));
    }

    @Test
    @DisplayName("POST /admin/themes - 본문의 name이 빈 문자열이면 400과 메시지를 반환한다")
    void createThemeReturns400WhenNameIsBlank() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "");
        body.put("description", "무서움");
        body.put("thumbnailImageUrl", "https://thumbnail.url");

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("DELETE /admin/themes/{id} - 200을 반환하고 서비스에 위임한다")
    void deleteThemeReturns200AndDelegates() throws Exception {
        mockMvc.perform(delete("/admin/themes/3"))
                .andExpect(status().isOk());

        verify(themeService).deleteTheme(3L);
    }

    @Test
    @DisplayName("DELETE /admin/themes - 서비스가 ResourceNotFoundException을 던지면 404과 메시지를 반환한다")
    void deleteThemeReturns404OnResourceNotFoundException() throws Exception {
        org.mockito.BDDMockito.willThrow(new RoomescapeException(ErrorType.RESOURCE_NOT_FOUND, "테마", 9999L))
                .given(themeService).deleteTheme(9999L);

        mockMvc.perform(delete("/admin/themes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
