package roomescape.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.fixture.DateFixture.getNextDay;

import jakarta.servlet.http.Cookie;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import roomescape.auth.controller.dto.MemberResponse;
import roomescape.auth.domain.AuthInfo;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.util.ControllerTest;

@DisplayName("관리자 API 통합 테스트")
class AdminControllerTest extends ControllerTest {

    @DisplayName("예약 목록 조회 시, 200을 반환한다.")
    @Test
    void getReservations() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse, getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doReturn(List.of(reservationResponse))
                .when(memberReservationService)
                .findMemberReservations(any());

        //then
        mockMvc.perform(
                get("/reservations")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("관리자 예약 생성 시, 201을 반환한다.")
    @Test
    void create() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse, getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", memberResponse.id());
        params.put("date", "2099-08-05");
        params.put("timeId", reservationTimeResponse.id());
        params.put("themeId", themeResponse.id());

        //when
        doReturn(reservationResponse)
                .when(memberReservationService)
                .createMemberReservation(any());

        //then
        mockMvc.perform(
                post("/admin/reservations")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("관리자 예약 삭제 시, 204를 반환한다.")
    @Test
    void deleteTest() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse reservationResponse = new ReservationResponse(4L,
                memberResponse, getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doNothing()
                .when(memberReservationService)
                .delete(isA(Long.class));

        //then
        mockMvc.perform(
                delete(String.format(
                        String.format("/admin/reservations/%d", reservationResponse.memberReservationId())))
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }


    @DisplayName("대기 예약을 승인할 경우, 200을 반환한다.")
    @Test
    void approveWaiting() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse waitingResponse = new ReservationResponse(4L,
                memberResponse, getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        doNothing()
                .when(waitingReservationService)
                .approveWaiting(isA(AuthInfo.class), isA(Long.class));

        //when & then
        mockMvc.perform(
                post(String.format("/admin/reservations/%d/waiting/approve", waitingResponse.memberReservationId()))
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("대기 예약을 거절할 경우, 200을 반환한다.")
    @Test
    void denyWaiting() throws Exception {
        //given
        MemberResponse memberResponse = new MemberResponse(1L, "초코칩");
        ReservationTimeResponse reservationTimeResponse = new ReservationTimeResponse(2L, LocalTime.NOON);
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");
        ReservationResponse waitingResponse = new ReservationResponse(4L,
                memberResponse, getNextDay(),
                reservationTimeResponse,
                themeResponse
        );

        //when
        doNothing()
                .when(waitingReservationService)
                .approveWaiting(isA(AuthInfo.class), isA(Long.class));

        //when & then
        mockMvc.perform(
                post(String.format("/admin/reservations/%d/waiting/deny", waitingResponse.memberReservationId()))
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }
}
