package roomescape.controller.user;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.domain.Reservation;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
import roomescape.service.ReservationService;
import roomescape.service.PaymentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 사용자_예약을_생성한다() throws Exception {
        given(paymentService.createForReservation(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class)))
                .willReturn(payment());

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/payments/1/checkout"))
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.checkoutUrl").value("/payments/1/checkout"));

        verify(paymentService, times(1)).createForReservation(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 결제_대기_예약의_결제를_다시_시도한다() throws Exception {
        given(paymentService.retryForReservation(eq(1L), eq("브라운"), any(LocalDateTime.class)))
                .willReturn(payment());

        mockMvc.perform(post("/reservations/{id}/payments", 1L).param("name", "브라운"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/payments/1/checkout"))
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.checkoutUrl").value("/payments/1/checkout"));

        verify(paymentService).retryForReservation(eq(1L), eq("브라운"), any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약을_조회한다() throws Exception {
        given(reservationService.findByName(eq("브라운")))
                .willReturn(List.of(reservation()));

        mockMvc.perform(get("/reservations")
                        .param("name", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("브라운"))
                .andExpect(jsonPath("$[0].date").value("2099-01-01"))
                .andExpect(jsonPath("$[0].time.id").value(1))
                .andExpect(jsonPath("$[0].time.startAt").value("10:00:00"))
                .andExpect(jsonPath("$[0].theme.id").value(1))
                .andExpect(jsonPath("$[0].theme.name").value("테마"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        verify(reservationService, times(1)).findByName("브라운");
        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약_조회시_이름이_비어있으면_에러_응답() throws Exception {
        mockMvc.perform(get("/reservations")
                        .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약을_취소한다() throws Exception {
        Long id = 1L;
        String name = "브라운";

        mockMvc.perform(delete("/reservations/{id}", id)
                        .param("name", name))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).deleteByUser(eq(id), eq(name), any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약_취소시_id가_양수가_아니면_에러_응답() throws Exception {
        mockMvc.perform(delete("/reservations/0")
                        .param("name", "브라운"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("id는 양수이어야 합니다."));

        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약을_변경한다() throws Exception {
        Long id = 1L;
        given(reservationService.updateByUser(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L),
                any(LocalDateTime.class)))
                .willReturn(updatedReservation());

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
                .andExpect(jsonPath("$.theme.name").value("테마"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(reservationService, times(1)).updateByUser(
                eq(id),
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 2)),
                eq(2L),
                any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약_변경시_유효하지_않은_입력값이면_에러_응답() throws Exception {
        String request = """
                {
                  "name": "",
                  "date": "2099-01-02",
                  "timeId": 2
                }
                """;

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("name은 비어 있을 수 없습니다."));

        verifyNoMoreInteractions(reservationService, paymentService);
    }

    @Test
    void 사용자_본인_예약_변경시_변경할_값이_없으면_에러_응답() throws Exception {
        String request = """
                {
                  "name": "브라운"
                }
                """;

        mockMvc.perform(put("/reservations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("변경할 날짜 또는 시간이 필요합니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 유효하지_않은_입력값이면_에러_응답() throws Exception {
        String request = """
                {
                  "name": "",
                  "date": "2099-01-01",
                  "timeId": 1,
                  "themeId": 1
                }
                """;

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
        String request = """
                {
                  "name": "브라운",
                  "date": "2099-01-01",
                  "timeId": "abc",
                  "themeId": 1
                }
                """;

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.detail").value("요청 본문 형식이 올바르지 않습니다."));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 일시적_DB_실패가_발생하면_재시도_가능한_에러_응답() throws Exception {
        given(paymentService.createForReservation(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class)))
                .willThrow(new CannotAcquireLockException("lock timeout"));

        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TEMPORARY_UNAVAILABLE"))
                .andExpect(jsonPath("$.detail").value("요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."));

        verify(paymentService, times(1)).createForReservation(
                eq("브라운"),
                eq(LocalDate.of(2099, 1, 1)),
                eq(1L),
                eq(1L),
                any(LocalDateTime.class));
        verifyNoMoreInteractions(reservationService, paymentService);
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
        return new Reservation(1L, new Reserver("브라운"), new ReservationSlot(LocalDate.of(2099, 1, 1), time, theme));
    }

    private Reservation updatedReservation() {
        ReservationTime time = new ReservationTime(2L, LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        return new Reservation(1L, new Reserver("브라운"), new ReservationSlot(LocalDate.of(2099, 1, 2), time, theme));
    }

    private Payment payment() {
        return new Payment(1L, 1L, "payment_12345678901234567890123456789012", 20_000L, null,
                PaymentStatus.READY, null, null);
    }
}
