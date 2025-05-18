package roomescape.reservationTime.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;
import roomescape.reservationTime.service.ReservationTimeService;

@WebMvcTest(ReservationTimeApiController.class)
class ReservationTimeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationTimeService reservationTimeService;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;

    private static final String URI = "/times";

    @DisplayName("시간 내역을 모두 조회한다")
    @Test
    void findAll() throws Exception {
        mockMvc.perform(get(URI)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("날짜와 테마에 따라 시간 내역을 모두 조회한다")
    @Test
    void findByDateAndTheme() throws Exception {
        mockMvc.perform(get(URI + "/available")
                        .param("date", LocalDate.now().toString())
                        .param("themeId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("추가하려는 시간이 빈 값일 경우 예외가 발생한다")
    @Test
    void exception_add_time_empty() throws Exception {
        String requestBody = """
                {
                    "startAt": ""
                }
                """;

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("추가하려는 시간이 형식에 맞지 않을 경우 예외가 발생한다")
    @Test
    void exception_add_time_format() throws Exception {
        String requestBody = """
                {
                    "startAt": "10"
                }
                """;

        mockMvc.perform(post("/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("해당 아이디의 시간 내역을 삭제한다")
    @Test
    void deleteById() throws Exception {
        mockMvc.perform(delete(URI + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
