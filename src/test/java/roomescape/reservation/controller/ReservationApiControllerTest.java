package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.GlobalExceptionHandler;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.MemberResponse;
import roomescape.member.login.authentication.WebMvcConfiguration;
import roomescape.member.login.authorization.JwtTokenProvider;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;
import roomescape.member.login.authorization.TokenAuthorizationHandler;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.user.UserReservationRequest;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.domain.Theme;

@WebMvcTest(ReservationApiController.class)
@Import({WebMvcConfiguration.class, GlobalExceptionHandler.class})
class ReservationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private TokenAuthorizationHandler tokenAuthorizationHandler;
    @MockitoBean
    private LoginAuthorizationInterceptor loginAuthorizationInterceptor;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final String URI = "/reservations";

    @DisplayName("예약 내역을 모두 조회한다")
    @Test
    void findAll() throws Exception {
        Reservation reservation = new Reservation(
                new Member(1L, "test", "test@test.com", "password", Role.USER),
                LocalDate.now().plusDays(1),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "테마1", "테마1 설명", "thumbnail.jpg")
        );

        List<ReservationResponse> expectedResponses = List.of(
                new ReservationResponse(
                        reservation.getId(),
                        reservation.getMember(),
                        reservation.getTheme(),
                        reservation.getDate(),
                        ReservationTimeResponse.from(reservation.getTime())
                )
        );
        when(reservationService.findAll()).thenReturn(expectedResponses);

        mockMvc.perform(get(URI)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("예약을 성공적으로 추가한다")
    @Test
    void add_success() throws Exception {
        UserReservationRequest reservationRequest = new UserReservationRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        ReservationResponse reservationResponse = mock(ReservationResponse.class);
        MemberResponse memberResponse = mock(MemberResponse.class);

        when(tokenAuthorizationHandler.extractToken(any()))
                .thenReturn("test-token");
        when(memberService.findByToken("test-token"))
                .thenReturn(memberResponse);
        when(reservationService.add(1L, reservationRequest))
                .thenReturn(reservationResponse);

        mockMvc.perform(post(URI)
                        .cookie(new Cookie("token", "test-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isCreated());
    }

    @DisplayName("추가하려는 예약 내역 속성에 빈 값이 있는 경우 잘못된 요청으로 처리한다")
    @Test
    void exception_add_empty() throws Exception {
        String requestBody = """
                {
                    "date": null,
                    "timeId": null,
                    "themeId": null
                }
                """;

        mockMvc.perform(post(URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("해당 아이디의 예약 내역을 삭제한다")
    @Test
    void deleteById() throws Exception {
        mockMvc.perform(delete(URI + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
