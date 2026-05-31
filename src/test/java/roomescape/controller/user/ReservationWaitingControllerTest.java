package roomescape.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationWaitingService;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationWaitingController.class)
class ReservationWaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationWaitingService reservationWaitingService;

    @Test
    void 예약_대기를_생성한다() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willReturn(waitingResult());

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/waitings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2099-01-01"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.theme.name").value("테마"))
                .andExpect(jsonPath("$.turn").value(2));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 이름으로_예약_대기_목록을_조회한다() throws Exception {
        // given
        given(reservationWaitingService.findByName(eq("브라운")))
                .willReturn(List.of(waitingResult()));

        // when & then
        mockMvc.perform(get("/waitings")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"))
                .andExpect(jsonPath("$[0].turn").value(2));

        verify(reservationWaitingService, times(1)).findByName("브라운");
    }

    @Test
    void 예약_대기를_취소한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .param("name", "브라운"))
                .andExpect(status().isNoContent());

        verify(reservationWaitingService, times(1)).delete(1L, "브라운");
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_생성시_유효하지_않은_입력값이면_에러_응답() throws Exception {
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
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_생성시_요청_본문_형식이_올바르지_않으면_에러_응답() throws Exception {
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
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("요청 본문 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_조회시_이름이_없으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/waitings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name는 필수입니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_조회시_이름이_비어있으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(get("/waitings")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_id가_양수가_아니면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/0")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_id_형식이_올바르지_않으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/abc")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_이름이_없으면_에러_응답() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name는 필수입니다."));

        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 이미_대기한_시간이면_에러_응답() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약 대기를 신청한 시간입니다."));

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("이미 예약 대기를 신청한 시간입니다."));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_가능한_시간이면_에러_응답() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.INVALID_INPUT, "예약 가능한 시간에는 대기를 신청할 수 없습니다."));

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("예약 가능한 시간에는 대기를 신청할 수 없습니다."));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 본인이_예약한_시간이면_에러_응답() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION, "본인이 예약한 시간에는 대기를 신청할 수 없습니다."));

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WAITING_NOT_ALLOWED_FOR_OWN_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("본인이 예약한 시간에는 대기를 신청할 수 없습니다."));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 지난_시간이면_에러_응답() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.PAST_RESERVATION, "이미 지난 시간으로는 예약 대기를 신청할 수 없습니다."));

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAST_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("이미 지난 시간으로는 예약 대기를 신청할 수 없습니다."));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 존재하지_않는_리소스이면_에러_응답() throws Exception {
        // given
        given(reservationWaitingService.create(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L)))
                .willThrow(new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 테마입니다."));

        verify(reservationWaitingService, times(1)).create(
                "브라운",
                LocalDate.of(2099, 1, 1),
                1L,
                1L);
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_본인의_대기가_아니면_에러_응답() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.FORBIDDEN_RESERVATION, "본인의 예약 대기만 취소할 수 있습니다."))
                .given(reservationWaitingService).delete(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .param("name", "브라운"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_RESERVATION"))
                .andExpect(jsonPath("$.detail").value("본인의 예약 대기만 취소할 수 있습니다."));

        verify(reservationWaitingService, times(1)).delete(1L, "브라운");
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_이미_지난_대기이면_에러_응답() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.PAST_RESERVATION_LOCKED, "이미 지난 예약 대기는 취소할 수 없습니다."))
                .given(reservationWaitingService).delete(1L, "브라운");

        // when & then
        mockMvc.perform(delete("/waitings/1")
                        .param("name", "브라운"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAST_RESERVATION_LOCKED"))
                .andExpect(jsonPath("$.detail").value("이미 지난 예약 대기는 취소할 수 없습니다."));

        verify(reservationWaitingService, times(1)).delete(1L, "브라운");
        verifyNoMoreInteractions(reservationWaitingService);
    }

    @Test
    void 예약_대기_취소시_존재하지_않는_대기이면_에러_응답() throws Exception {
        // given
        willThrow(new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 대기입니다."))
                .given(reservationWaitingService).delete(999L, "브라운");

        // when & then
        mockMvc.perform(delete("/waitings/999")
                        .param("name", "브라운"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("존재하지 않는 예약 대기입니다."));

        verify(reservationWaitingService, times(1)).delete(999L, "브라운");
        verifyNoMoreInteractions(reservationWaitingService);
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

    private WaitingResult waitingResult() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new WaitingResult(1L, "브라운", LocalDate.of(2099, 1, 1), time, theme, 2L);
    }
}
