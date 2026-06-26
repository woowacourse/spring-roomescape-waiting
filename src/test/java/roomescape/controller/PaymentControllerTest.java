package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.dto.PaymentCancelRequest;
import roomescape.controller.dto.PaymentConfirmRequest;
import roomescape.domain.ReservationStatus;
import roomescape.payment.PaymentService;
import roomescape.payment.order.PaymentOrderHistory;
import roomescape.payment.order.PaymentOrderStatus;
import roomescape.service.UserReservationService;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.dto.ThemeResult;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserReservationService userReservationService;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /user/payments/confirm - 결제가 승인되면 예약을 반환한다")
    void confirm() throws Exception {
        given(userReservationService.confirm(any())).willReturn(sampleResult());
        PaymentConfirmRequest request = new PaymentConfirmRequest("test_pk_1", "order-1", 1000L);

        mockMvc.perform(post("/user/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reserverName").value("브라운"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /user/payments/cancel - orderId가 있으면 결제 대기 주문을 취소하고 204를 반환한다")
    void cancel() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest("REJECT_CARD_PAYMENT", "카드 거절", "order-1");

        mockMvc.perform(post("/user/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userReservationService).cancelOrder("order-1");
    }

    @Test
    @DisplayName("POST /user/payments/cancel - orderId가 없으면(사용자 취소) 주문 취소 없이 204를 반환한다")
    void cancel_withoutOrderId() throws Exception {
        PaymentCancelRequest request = new PaymentCancelRequest("PAY_PROCESS_CANCELED", "사용자 취소", null);

        mockMvc.perform(post("/user/payments/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userReservationService, never()).cancelOrder(any());
    }

    @Test
    @DisplayName("GET /user/payments - 결제 대기/확인 필요 주문을 포함한 내역을 반환한다")
    void history() throws Exception {
        given(paymentService.findOrderHistories("브라운")).willReturn(List.of(
                new PaymentOrderHistory("order-1", LocalDate.of(2099, 12, 31), LocalTime.of(10, 0),
                        "무인도 탈출", 1000L, "test_pk_1", PaymentOrderStatus.CONFIRMED),
                new PaymentOrderHistory("order-2", LocalDate.of(2099, 12, 31), LocalTime.of(11, 0),
                        "공포의 방", 1000L, null, PaymentOrderStatus.UNKNOWN)
        ));

        mockMvc.perform(get("/user/payments").param("reserverName", "브라운"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].paymentStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$[1].paymentStatus").value("UNKNOWN"))
                .andExpect(jsonPath("$[1].paymentKey").doesNotExist());
    }

    private ReservationResult sampleResult() {
        return new ReservationResult(
                1L, "브라운", LocalDate.of(2099, 12, 31),
                new ReservationTimeResult(1L, LocalTime.of(10, 0)),
                new ThemeResult(1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg"),
                0L,
                ReservationStatus.CONFIRMED
        );
    }
}
