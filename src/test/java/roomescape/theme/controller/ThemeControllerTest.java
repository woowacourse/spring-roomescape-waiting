package roomescape.theme.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.domain.Member;
import roomescape.member.security.crypto.JwtTokenProvider;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

@WebMvcTest(ThemeController.class)
public class ThemeControllerTest {

    private final Theme theme = new Theme(1L, "포레스트", "공포 테마",
            "https://zerogangnam.com/storage/AVISPw8N2JfMThKvnk3VJzeY9qywIaYd8pTy46Xx.jpg");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThemeService themeService;
    @MockBean
    private MemberService memberService;
    @MockBean
    private MemberAuthService memberAuthService;

    private String token;

    @BeforeEach
    void setUp() {
        Member member = new Member("valid", "e@m.com", "pass");
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("secret-key", 99999999999L);
        token = jwtTokenProvider.createToken(member, new Date());
    }


    @Test
    @DisplayName("테마 정보를 정상적으로 저장하는지 확인한다.")
    void saveTheme() throws Exception {
        Mockito.when(themeService.addTheme(any()))
                .thenReturn(ThemeResponse.fromTheme(theme));

        String content = new ObjectMapper().writeValueAsString(
                new ThemeRequest(theme.getName(), theme.getDescription(), theme.getThumbnail()));

        mockMvc.perform(post("/themes").cookie(new Cookie("token", token)).content(content)
                        .contentType("application/Json")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예약 정보를 정상적으로 불러오는지 롹인한다.")
    void findAllThemes() throws Exception {
        Mockito.when(themeService.findThemes())
                .thenReturn(List.of(ThemeResponse.fromTheme(theme)));

        mockMvc.perform(get("/themes"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인기많은 테마 정보를 정상적으로 가져오는지 확인한다.")
    void findTopRankThemes() throws Exception {
        Mockito.when(themeService.findRankedThemes())
                .thenReturn(List.of(ThemeRankResponse.fromTheme(theme)));

        mockMvc.perform(get("/themes/rank"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("예약 정보를 정상적으로 지우는지 확인한다.")
    void deleteTheme() throws Exception {
        mockMvc.perform(delete("/themes/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

}
