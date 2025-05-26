package roomescape.theme;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.JwtProvider;
import roomescape.auth.TokenBody;
import roomescape.member.MemberRole;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private JwtProvider jwtProvider;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ThemeService themeService() {
            return mock(ThemeService.class);
        }

        @Bean
        public JwtProvider jwtProvider() {
            return mock(JwtProvider.class);
        }
    }

    @Test
    @DisplayName("테마 생성 요청에 성공할 경우 201을 응답한다")
    void create() throws Exception {
        // given
        Map<String, Object> memberClaims = new HashMap<>();
        memberClaims.put("role", MemberRole.ADMIN.name());
        Claims claims = new DefaultClaims(memberClaims);
        given(jwtProvider.isValidToken(any()))
                .willReturn(true);
        given(jwtProvider.extractBody(any()))
                .willReturn(new TokenBody(claims));

        ThemeRequest request = new ThemeRequest("테마명", "테마 설명", "abc");
        ThemeResponse response = new ThemeResponse(1L, "테마명", "테마 설명", "abc");
        given(themeService.create(request)).willReturn(response);

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/themes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("테마명"))
                .andExpect(jsonPath("$.description").value("테마 설명"))
                .andExpect(jsonPath("$.thumbnail").value("abc"));
    }

    @Test
    @DisplayName("모든 테마를 조회하면 200과 테마 목록을 응답한다")
    void readAll() throws Exception {
        // given
        List<ThemeResponse> responses = List.of(
                new ThemeResponse(1L, "테마1", "설명1", "abc"),
                new ThemeResponse(2L, "테마2", "설명2", "abc")
        );
        given(themeService.getAll()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테마1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("테마2"));
    }

    @Test
    @DisplayName("테마가 없을 경우 빈 목록을 응답한다")
    void readAllEmpty() throws Exception {
        // given
        given(themeService.getAll()).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("랭킹 테마를 조회하면 200과 테마 목록을 응답한다")
    void readTopRankThemes() throws Exception {
        // given
        List<ThemeResponse> responses = List.of(
                new ThemeResponse(1L, "인기테마1", "설명1", "http://example.com/1.jpg"),
                new ThemeResponse(2L, "인기테마2", "설명2", "http://example.com/2.jpg")
        );
        given(themeService.getTopRankThemes(anyInt())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/themes/ranking")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("인기테마1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("인기테마2"));
    }

    @Test
    @DisplayName("테마 삭제 요청에 성공할 경우 204를 응답한다")
    void deleteById() throws Exception {
        // when & then
        mockMvc.perform(delete("/themes/1"))
                .andExpect(status().isNoContent());

        verify(themeService).deleteById(1L);
    }
}
