package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.TestFixture;
import roomescape.auth.CookieProvider;
import roomescape.auth.JwtTokenProvider;
import roomescape.controller.request.CreateWaitingRequest;
import roomescape.service.MemberService;
import roomescape.service.WaitingService;
import roomescape.service.param.CreateWaitingParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.WaitingResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CookieProvider cookieProvider;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private WaitingService waitingService;

    @MockitoBean
    private MemberService memberService;

    private MemberResult memberResult;
    private Cookie authCookie;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        memberResult = TestFixture.createMemberResult();
        authCookie = new Cookie(TestFixture.AUTH_COOKIE_NAME, TestFixture.VALID_TOKEN);

        when(cookieProvider.extractTokenFromCookies(any())).thenReturn(TestFixture.VALID_TOKEN);
        when(jwtTokenProvider.extractIdFromToken(TestFixture.VALID_TOKEN)).thenReturn(TestFixture.TEST_MEMBER_ID);
        when(memberService.findById(TestFixture.TEST_MEMBER_ID)).thenReturn(memberResult);
        when(cookieProvider.extractTokenFromCookie(any(Cookie.class))).thenReturn(TestFixture.VALID_TOKEN);
    }

    @Test
    @DisplayName("전체 예약 대기 목록을 조회할 수 있다.")
    void findWaitings() throws Exception {
        //given
        WaitingResult waitingResult = TestFixture.createDefaultWaitingResult();
        when(waitingService.findAll()).thenReturn(List.of(waitingResult));

        //when & then
        mockMvc.perform(get("/waitings"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(waitingResult.id()));
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void deleteWaiting() throws Exception {
        //given
        doNothing().when(waitingService).deleteById(TestFixture.TEST_WAITING_ID);

        //when & then
        mockMvc.perform(delete("/waitings/" + TestFixture.TEST_WAITING_ID)
                .cookie(authCookie))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void createWaiting() throws Exception {
        //given
        CreateWaitingRequest request = new CreateWaitingRequest(
                TestFixture.TEST_DATE,
                TestFixture.TEST_TIME_ID,
                TestFixture.TEST_THEME_ID
        );
        WaitingResult waitingResult = TestFixture.createDefaultWaitingResult();
        when(waitingService.create(any(CreateWaitingParam.class)))
                .thenReturn(waitingResult);
        //when & then
        mockMvc.perform(post("/waitings")
                .cookie(authCookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(waitingResult.id()));
    }
}