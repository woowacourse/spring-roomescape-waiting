package roomescape.waiting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.auth.web.interceptor.AdminMemberHandlerInterceptor;
import roomescape.auth.web.resolver.AuthenticatedMemberArgumentResolver;
import roomescape.config.WebMvcTestConfig;
import roomescape.fixture.entity.MemberFixture;
import roomescape.fixture.entity.ReservationDateTimeFixture;
import roomescape.fixture.entity.ThemeFixture;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.request.ReserveByUserRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.service.WaitingService;

@WebMvcTest(controllers = WaitingApiController.class)
@Import({WebMvcTestConfig.class})
class WaitingApiControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WaitingService waitingService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private AdminMemberHandlerInterceptor adminMemberHandlerInterceptor;

    @MockitoBean
    private AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;

    @Test
    void 예약_대기를_생성한다() throws Exception {
        // given
        Member user = MemberFixture.createUser();
        ReservationDateTime reservationDateTime = ReservationDateTimeFixture.create();
        ReservationTime time = reservationDateTime.getReservationTime();
        LocalDate date = reservationDateTime.getDate();
        Theme theme = ThemeFixture.create();
        MemberResponse memberResponse = MemberResponse.from(user);

        ReserveByUserRequest reserveByUserRequest = new ReserveByUserRequest(date, 1L, 1L);

        ReservationResponse reservationResponse = new ReservationResponse(1L, memberResponse, date,
                ReservationTimeResponse.from(time), ThemeResponse.from(theme));

        given(reservationService.waiting(any())).willReturn(reservationResponse);

        // when & then
        mockMvc.perform(post("/reservations/waiting").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveByUserRequest))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L)).andDo(print());
    }

    @Test
    void 이미_예약한_사람이면_409_상태_코드를_반환한다() throws Exception {
        // given
        ReserveByUserRequest reserveByUserRequest = new ReserveByUserRequest(LocalDate.now(), 1L, 1L);
        given(reservationService.waiting(any()))
                .willThrow(new InAlreadyReservationException("이미 예약한 사용자입니다."));

        // when & then
        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveByUserRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void 이미_예약_대기를_했다면_409_상태_코드를_반환한다() throws Exception {
        // given
        ReserveByUserRequest reserveByUserRequest = new ReserveByUserRequest(LocalDate.now(), 1L, 1L);
        given(reservationService.waiting(any()))
                .willThrow(new InAlreadyWaitingException("이미 예약 대기를 한 사용자입니다."));

        // when & then
        mockMvc.perform(post("/reservations/waiting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveByUserRequest)))
                .andExpect(status().isConflict());
    }
}
