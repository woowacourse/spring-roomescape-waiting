package roomescape.controller;

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
import roomescape.infrastructure.AdminAuthorizationInterceptor;
import roomescape.infrastructure.LoginCheckInterceptor;
import roomescape.infrastructure.LoginUserArgumentResolver;
import roomescape.infrastructure.WebConfig;
import roomescape.service.ThemeService;

@WebMvcTest(controllers = AdminThemeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {WebConfig.class, LoginCheckInterceptor.class,
                        AdminAuthorizationInterceptor.class, LoginUserArgumentResolver.class}))
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void POST_admin_themes_생성된_id를_Location_헤더에_담아_201을_반환한다() throws Exception {
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
    void POST_admin_themes_본문의_name이_빈_문자열이면_400과_메시지를_반환한다() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "");
        body.put("description", "무서움");
        body.put("thumbnailImageUrl", "https://thumbnail.url");

        mockMvc.perform(post("/admin/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("name은(는) 필수 입력값입니다."));
    }

    @Test
    void DELETE_admin_themes_id_200을_반환하고_서비스에_위임한다() throws Exception {
        mockMvc.perform(delete("/admin/themes/3"))
                .andExpect(status().isOk());

        verify(themeService).deleteTheme(3L);
    }

    @Test
    void DELETE_admin_themes_서비스가_ResourceNotFoundException을_던지면_404과_메시지를_반환한다() throws Exception {
        org.mockito.BDDMockito.willThrow(new roomescape.exception.ResourceNotFoundException("테마", 9999L))
                .given(themeService).deleteTheme(9999L);

        mockMvc.perform(delete("/admin/themes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("테마을(를) 찾을 수 없습니다. id=9999"));
    }
}