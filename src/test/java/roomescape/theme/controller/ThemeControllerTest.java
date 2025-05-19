package roomescape.theme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import roomescape.auth.infrastructure.handler.AuthorizationHandler;
import roomescape.auth.infrastructure.methodargument.AuthorizationPrincipalInterceptor;
import roomescape.auth.infrastructure.methodargument.CheckMemberRoleInterceptor;
import roomescape.auth.infrastructure.methodargument.LoginMemberArgumentResolver;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;
import roomescape.theme.service.ThemeServiceFacade;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeServiceFacade themeServiceFacade;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @MockitoBean
    private CheckMemberRoleInterceptor checkMemberRoleInterceptor;

    @MockitoBean
    private AuthorizationPrincipalInterceptor authorizationPrincipalInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(authorizationPrincipalInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void 테마_생성_확인() throws Exception {
        ThemeCreateRequest request = new ThemeCreateRequest("Test Theme", "Test Description", "Test Thumbnail");
        ThemeResponse response = new ThemeResponse(1L, "Test Theme", "Test Description", "Test Thumbnail");

        when(themeServiceFacade.createTheme(any(ThemeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Theme"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.thumbnail").value("Test Thumbnail"));
    }

    @Test
    void 테마_목록_불러오기() throws Exception {
        List<ThemeResponse> responses = createThemeResponses();
        when(themeServiceFacade.findAll()).thenReturn(responses);
        mockMvc.perform(MockMvcRequestBuilders.get("/themes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(responses.get(0).id()))
                .andExpect(jsonPath("$[0].name").value(responses.get(0).name()))
                .andExpect(jsonPath("$[0].description").value(responses.get(0).description()))
                .andExpect(jsonPath("$[0].thumbnail").value(responses.get(0).thumbnail()))
                .andExpect(jsonPath("$[1].id").value(responses.get(1).id()))
                .andExpect(jsonPath("$[1].name").value(responses.get(1).name()))
                .andExpect(jsonPath("$[1].description").value(responses.get(1).description()))
                .andExpect(jsonPath("$[1].thumbnail").value(responses.get(1).thumbnail()));
    }
    
    private List<ThemeResponse> createThemeResponses() {
        return List.of(
                new ThemeResponse(1L, "Test Theme", "Test Description", "Test Thumbnail"),
                new ThemeResponse(2L, "Test Theme2", "Test Description2", "Test Thumbnail2")
        );
                                                                                                      }

    @Test
    void 테마_삭제하기() throws Exception {
        // given
        Long themeId = 1L;
        doNothing().when(themeServiceFacade).deleteThemeById(themeId);

        // when & then
        mockMvc.perform(delete("/themes/{id}", themeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(themeServiceFacade, times(1)).deleteThemeById(themeId);
    }
}

