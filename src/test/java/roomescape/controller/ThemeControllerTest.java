package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.config.TestWebmvcConfiguration;
import roomescape.controller.request.CreateThemeRequest;
import roomescape.domain.MemberRole;
import roomescape.domain.Theme;
import roomescape.service.MemberService;
import roomescape.service.ThemeService;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ThemeResult;

@WebMvcTest(ThemeController.class)
@Import({JwtTokenProvider.class, CookieProvider.class})
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private MemberService memberService;

    private Cookie getAdminCookie() {
        MemberResult memberResult = new MemberResult(1L, "히스타", MemberRole.ADMIN, "admin@email.com");
        String token = jwtTokenProvider.createToken(memberResult);
        return new Cookie("token", token);
    }

    @DisplayName("GET: /themes 요청이 들어오면 200을 응답한다")
    @Test
    void findAllTest() throws Exception {
        // given
        BDDMockito.given(themeService.findAll())
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk());
    }

    @DisplayName("POST: /themes 요청이 들어오면 201을 응답한다")
    @Test
    void createTest() throws Exception {
        // given
        CreateThemeRequest request = new CreateThemeRequest("이름", "설명", "썸네일");
        BDDMockito.given(themeService.create(request.toServiceParam())).willReturn(1L);
        BDDMockito.given(themeService.findById(1L)).willReturn(
                ThemeResult.from(new Theme(1L, "이름", "설명", "썸네일")));

        // when & then
        mockMvc.perform(
                post("/themes")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated());
    }

    @DisplayName("DELETE: /themes/{themeId} 요청이 어드민 권한과 함께 들어오면 204를 응답한다")
    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(delete("/themes/{themeId}", 1L).cookie(getAdminCookie()))
                .andExpect(status().isNoContent());
    }

    @DisplayName("GET: /themes/rank 요청이 들어오면 200을 응답한다")
    @Test
    void rankTest() throws Exception {
        // given
        BDDMockito.given(themeService.findRankByTheme())
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/themes/rank"))
                .andExpect(status().isOk());
    }
}
