package roomescape.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 사용자_예약을_생성한다() throws Exception {
        // given
        given(reservationService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willReturn(reservation());

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2099-01-01"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테마"));

        verify(reservationService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약을_조회한다() throws Exception {
        // given
        given(reservationService.findByName(eq("브라운")))
                .willReturn(List.of(reservation()));

        // when & then
        mockMvc.perform(get("/reservations")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"));

        verify(reservationService, times(1)).findByName("브라운");
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_조회시_이름이_없으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name는 필수입니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_조회시_이름이_비어있으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/reservations")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약을_취소한다() throws Exception {
        // given
        Long id = 1L;
        String name = "브라운";

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id)
                        .param("name", name))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).delete(id, name);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_이름이_없으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name는 필수입니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_이름이_비어있으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/1")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/0")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_id_형식이_올바르지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/reservations/abc")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_존재하지_않는_예약이면_에러_응답() throws Exception {
        // given
        Long id = 999L;
        String name = "브라운";
        willThrow(new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."))
                .given(reservationService).delete(id, name);

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id)
                        .param("name", name))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 예약입니다."));

        verify(reservationService, times(1)).delete(id, name);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_본인의_예약이_아니면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        String name = "브라운";
        willThrow(new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약만 변경하거나 취소할 수 있습니다."))
                .given(reservationService).delete(id, name);

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id)
                        .param("name", name))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_RESOURCE"))
                .andExpect(jsonPath("$.detail").value("본인의 예약만 변경하거나 취소할 수 있습니다."));

        verify(reservationService, times(1)).delete(id, name);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_취소시_이미_지난_예약이면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        String name = "브라운";
        willThrow(new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약은 변경하거나 취소할 수 없습니다."))
                .given(reservationService).delete(id, name);

        // when & then
        mockMvc.perform(delete("/reservations/{id}", id)
                        .param("name", name))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAST_RESOURCE_LOCKED"))
                .andExpect(jsonPath("$.detail").value("이미 지난 예약은 변경하거나 취소할 수 없습니다."));

        verify(reservationService, times(1)).delete(id, name);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약을_변경한다() throws Exception {
        // given
        Long id = 1L;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L)))
                .willReturn(updatedReservation());

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2099-01-02"))
                .andExpect(jsonPath("$.time.id").value(2))
                .andExpect(jsonPath("$.time.startAt").value("12:00:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테마"));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_유효하지_않은_입력값이면_에러_응답() throws Exception {
        // given
        String request = """
                {
                  "name": "",
                  "date": "2099-01-02",
                  "timeId": 2
                }
                """;

        // when & then
        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(put("/reservations/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_id_형식이_올바르지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(put("/reservations/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_존재하지_않는_예약이면_에러_응답() throws Exception {
        // given
        Long id = 999L;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L)))
                .willThrow(new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 예약입니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_본인의_예약이_아니면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        willThrow(new RoomescapeException(ErrorCode.FORBIDDEN_RESOURCE, "본인의 예약만 변경하거나 취소할 수 있습니다."))
                .given(reservationService).update(
                        id,
                        "브라운",
                        LocalDate.of(2099, 1, 2),
                        2L);

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_RESOURCE"))
                .andExpect(jsonPath("$.detail").value("본인의 예약만 변경하거나 취소할 수 있습니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_이미_지난_예약이면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L)))
                .willThrow(new RoomescapeException(ErrorCode.PAST_RESOURCE_LOCKED, "이미 지난 예약은 변경하거나 취소할 수 없습니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAST_RESOURCE_LOCKED"))
                .andExpect(jsonPath("$.detail").value("이미 지난 예약은 변경하거나 취소할 수 없습니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_지난_날짜_시간이면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        String request = """
                {
                  "name": "브라운",
                  "date": "2000-01-01"
                }
                """;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2000, 1, 1)),
                eq(null)))
                .willThrow(new RoomescapeException(ErrorCode.PAST_SCHEDULE, "이미 지난 시간으로는 예약할 수 없습니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAST_SCHEDULE"))
                .andExpect(jsonPath("$.detail").value("이미 지난 시간으로는 예약할 수 없습니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2000, 1, 1),
                null);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_이미_예약된_시간이면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L)))
                .willThrow(new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.detail").value("이미 예약된 시간입니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_기존_날짜와_시간이면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L)))
                .willThrow(new RoomescapeException(ErrorCode.UNCHANGED_RESERVATION, "기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UNCHANGED_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                LocalDate.of(2099, 1, 2),
                2L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 사용자_본인_예약_변경시_변경할_값이_없으면_에러_응답() throws Exception {
        // given
        Long id = 1L;
        String request = """
                {
                  "name": "브라운"
                }
                """;
        given(reservationService.update(
                eq(id),
                eq("브라운"),
                eq(null),
                eq(null)))
                .willThrow(new RoomescapeException(ErrorCode.INVALID_INPUT, "변경할 날짜 또는 시간이 필요합니다."));

        // when & then
        mockMvc.perform(put("/reservations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("변경할 날짜 또는 시간이 필요합니다."));

        verify(reservationService, times(1)).update(
                id,
                "브라운",
                null,
                null);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 유효하지_않은_입력값이면_에러_응답() throws Exception {
        // given
        String request = """
                {
                  "name": "",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 요청_본문_형식이_올바르지_않으면_에러_응답() throws Exception {
        // given
        String request = """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": "abc",
                  "themeId": 1
                }
                """;

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("요청 본문 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 지난_예약이면_에러_응답() throws Exception {
        // given
        given(reservationService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new RoomescapeException(ErrorCode.PAST_SCHEDULE, "이미 지난 시간으로는 예약할 수 없습니다."));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAST_SCHEDULE"))
                .andExpect(jsonPath("$.detail").value("이미 지난 시간으로는 예약할 수 없습니다."));

        verify(reservationService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 존재하지_않는_리소스이면_에러_응답() throws Exception {
        // given
        given(reservationService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 테마입니다."));

        verify(reservationService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 중복_예약이면_에러_응답() throws Exception {
        // given
        given(reservationService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다."));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.detail").value("이미 예약된 시간입니다."));

        verify(reservationService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 예상하지_못한_예외면_서버_에러_응답() throws Exception {
        // given
        given(reservationService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new RuntimeException("unexpected"));

        // when & then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.detail").value("서버에 문제가 발생했습니다."));

        verify(reservationService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationService);
    }

    private String validRequest() {
        return """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;
    }

    private String updateRequest() {
        return """
                {
                  "name": "브라운",
                  "date": "2099-01-02",
                  "timeId": 2
                }
                """;
    }

    private Reservation reservation() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(1L, "브라운", LocalDate.of(2099, 1, 1), time, theme);
    }

    private Reservation updatedReservation() {
        ReservationTime time = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(1L, "브라운", LocalDate.of(2099, 1, 2), time, theme);
    }
}
