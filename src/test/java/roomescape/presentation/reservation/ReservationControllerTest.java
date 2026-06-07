package roomescape.presentation.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.request.ReservationCreateRequest;
import roomescape.application.reservation.response.ReservationCreateResponse;
import roomescape.application.reservation.response.UserReservationsResponse;
import roomescape.common.auth.LoginUserArgumentResolver;
import roomescape.common.auth.SessionKeys;
import roomescape.common.config.AuthWebConfig;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;
import roomescape.presentation.error.GlobalExceptionHandler;

@DisplayName("예약 컨트롤러")
@WebMvcTest(controllers = ReservationController.class)
@Import({GlobalExceptionHandler.class, AuthWebConfig.class, LoginUserArgumentResolver.class})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    @DisplayName("예약자 이름으로 예약 목록을 조회할 수 있다")
    void getUserReservations() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        User loginUser = User.of(10L, "홍길동", "", UserRole.USER);
        session.setAttribute(SessionKeys.LOGIN_USER, loginUser);

        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 2),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                0,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationService.getUserReservations(loginUser))
                .willReturn(UserReservationsResponse.of("홍길동", List.of(reservation)));

        // when & then
        mockMvc.perform(get("/reservations").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("홍길동"))
                .andExpect(jsonPath("$.reservations[0].id").value(1))
                .andExpect(jsonPath("$.reservations[0].slot.date").value("2030-01-02"))
                .andExpect(jsonPath("$.reservations[0].slot.startAt.startAt").value("13:00"))
                .andExpect(jsonPath("$.reservations[0].slot.theme.name").value("도심 탈출"))
                .andExpect(jsonPath("$.reservations[0].waitingNumber").value(0))
                .andExpect(jsonPath("$.reservations[0].status").value("CONFIRMED"));

        verify(reservationService, times(1)).getUserReservations(loginUser);
    }

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createReservation() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        User loginUser = User.of(10L, "홍길동", "", UserRole.USER);
        session.setAttribute(SessionKeys.LOGIN_USER, loginUser);
        Reservation reservation = Reservation.of(
                1L,
                User.of(10L, "홍길동"),
                ReservationSlot.of(
                        20L,
                        LocalDate.of(2030, 1, 1),
                        ReservationTime.of(30L, LocalTime.of(13, 0)),
                        Theme.of(40L, "도심 탈출", "도심 탈출 설명", "/themes/40")
                ),
                null,
                ReservationStatus.WAITING,
                LocalDateTime.of(2030, 1, 1, 10, 0)
        );
        given(reservationService.createReservationByUser(any(ReservationCreateRequest.class), eq(loginUser)))
                .willReturn(ReservationCreateResponse.from(reservation));

        // when & then
        mockMvc.perform(post("/reservations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slotId": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").value("2030-01-01"))
                .andExpect(jsonPath("$.startAt").value("13:00"))
                .andExpect(jsonPath("$.theme.id").value(40))
                .andExpect(jsonPath("$.theme.name").value("도심 탈출"))
                .andExpect(jsonPath("$.theme.content").value("도심 탈출 설명"))
                .andExpect(jsonPath("$.theme.url").value("/themes/40"));

        verify(reservationService, times(1)).createReservationByUser(any(ReservationCreateRequest.class),
                eq(loginUser));
    }

    @Test
    @DisplayName("잘못된 예약 요청이면 필드 에러를 내려준다")
    void createReservationWhenValidationFails() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionKeys.LOGIN_USER, User.of(10L, "홍길동", "", UserRole.USER));

        // when & then
        mockMvc.perform(post("/reservations")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "홍길동"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.INPUT_FORMAT_ERROR.name()))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("slotId"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("슬롯은 필수 선택 사항 입니다. 슬롯을 선택해주세요."));

        verify(reservationService, never()).createReservationByUser(any(ReservationCreateRequest.class),
                any(User.class));
    }

    @Test
    @DisplayName("예약을 취소할 수 있다")
    void cancelReservation() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        User loginUser = User.of(10L, "홍길동", "", UserRole.USER);
        session.setAttribute(SessionKeys.LOGIN_USER, loginUser);

        // when & then
        mockMvc.perform(delete("/reservations/{id}", 1L).session(session))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).cancelReservationByUser(1L, loginUser);
    }
}
