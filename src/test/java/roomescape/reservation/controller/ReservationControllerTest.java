package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.fixture.DateFixture.getNextDay;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.auth.controller.dto.MemberResponse;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.util.ControllerTest;

@DisplayName("예약 API 통합 테스트")
class ReservationControllerTest extends ControllerTest {

    @DisplayName("사용자 예약 생성 시 201을 반환한다.")
    @Test
    void create() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2099-08-05");
        params.put("timeId", reservationTimeResponse.id());
        params.put("themeId", themeResponse.id());

        //when
        doReturn(reservationResponse)
                .when(reservationApplicationService)
                .createMemberReservation(any());

        //then
        mockMvc.perform(
                post("/reservations")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("예약 삭제 시 204를 반환한다.")
    @Test
    void deleteTest() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doNothing()
                .when(reservationApplicationService)
                .deleteMemberReservation(isA(AuthInfo.class), isA(Long.class));

        //then
        mockMvc.perform(
                delete("/reservations/" + reservationResponse.memberReservationId())
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("타인의 예약 삭제 시, 403을 반환한다.")
    @Test
    void delete_InvalidUser() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doThrow(new AuthorizationException(ErrorType.NOT_A_RESERVATION_MEMBER))
                .when(reservationApplicationService)
                .deleteMemberReservation(isA(AuthInfo.class), isA(Long.class));

        //then
        mockMvc.perform(
                delete("/reservations/" + reservationResponse.memberReservationId())
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @DisplayName("예약 조회 시 200을 반환한다.")
    @Test
    void find() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doReturn(List.of(reservationResponse))
                .when(reservationApplicationService)
                .findMemberReservations(any());

        //then
        mockMvc.perform(
                get("/reservations")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("지나간 날짜와 시간에 대한 예약 생성 시, 400을 반환한다.")
    @Test
    void createReservationAfterNow() throws Exception {
        //given
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().minusDays(2).toString());
        params.put("timeId", reservationTimeResponse.id());
        params.put("themeId", themeResponse.id());

        //when
        doThrow(new BadRequestException(ErrorType.INVALID_REQUEST_ERROR))
                .when(reservationApplicationService)
                .createMemberReservation(any());

        //then
        mockMvc.perform(
                post("/reservations")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("예약 대기 시, 201을 반환한다.")
    @Test
    void createWaiting() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        Map<String, Object> params = new HashMap<>();
        params.put("date", reservationResponse.date().toString());
        params.put("timeId", reservationResponse.time().id());
        params.put("themeId", reservationResponse.theme().id());

        //when
        doReturn(reservationResponse)
                .when(reservationApplicationService)
                .addWaiting(any());

        //then
        mockMvc.perform(
                post("/reservations/waiting")
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("예약 대기 삭제 시, 204을 반환한다.")
    @Test
    void deleteWaiting() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse,
                getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doReturn(reservationResponse)
                .when(reservationApplicationService)
                .addWaiting(any());

        //then
        mockMvc.perform(
                delete(String.format("/reservations/%d/waiting", reservationResponse.memberReservationId()))
                        .cookie(new Cookie("token", memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }
}
