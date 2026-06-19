package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.payment.PaymentConfirmRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.CardRejectedException;
import roomescape.exception.PaymentException.InvalidPaymentRequestException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;
import roomescape.exception.PaymentException.PaymentAuthException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentNotFoundException;
import roomescape.exception.PaymentException.PaymentResultUnknownException;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentRestController.class)
class PaymentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private final PaymentConfirmRequest request = new PaymentConfirmRequest("pk_test", "order-1", 10000);

    private ResultActions perform() throws Exception {
        return mockMvc.perform(post("/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    void 결제_승인에_성공하면_200과_예약_정보를_반환한다() throws Exception {
        ReservationResponse response = new ReservationResponse(
                1L, "테스트", LocalDate.now().plusDays(1),
                ReservationTimeResponse.from(new ReservationTime(1L, LocalTime.parse("10:00"))),
                ThemeResponse.from(new Theme(2L, "test", "설명", "url")),
                LocalDateTime.now(), true);
        given(paymentService.confirm(any())).willReturn(response);

        perform().andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트"))
                .andExpect(jsonPath("$.paid").value(true));
    }

    @Test
    void 금액_불일치면_400과_PAYMENT_AMOUNT_MISMATCH를_반환한다() throws Exception {
        willThrow(new PaymentAmountMismatchException("금액 불일치")).given(paymentService).confirm(any());

        perform().andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_AMOUNT_MISMATCH"));
    }

    @Test
    void 카드_거절이면_400과_CARD_REJECTED를_반환한다() throws Exception {
        willThrow(new CardRejectedException("카드 거절")).given(paymentService).confirm(any());

        perform().andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CARD_REJECTED"));
    }

    @Test
    void 중복_만료_요청이면_400과_INVALID_PAYMENT_REQUEST를_반환한다() throws Exception {
        willThrow(new InvalidPaymentRequestException("잘못된 요청")).given(paymentService).confirm(any());

        perform().andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PAYMENT_REQUEST"));
    }

    @Test
    void 이미_처리된_결제면_409와_ALREADY_PROCESSED_PAYMENT를_반환한다() throws Exception {
        willThrow(new AlreadyProcessedException("이미 처리됨")).given(paymentService).confirm(any());

        perform().andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_PROCESSED_PAYMENT"));
    }

    @Test
    void 결제건이_없으면_404와_PAYMENT_NOT_FOUND를_반환한다() throws Exception {
        willThrow(new PaymentNotFoundException("결제 없음")).given(paymentService).confirm(any());

        perform().andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    void 키_설정_오류면_500과_PAYMENT_CONFIG_ERROR를_반환한다() throws Exception {
        willThrow(new PaymentAuthException("키 오류")).given(paymentService).confirm(any());

        perform().andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_CONFIG_ERROR"));
    }

    @Test
    void 토스_내부_오류면_502와_PAYMENT_GATEWAY_ERROR를_반환한다() throws Exception {
        willThrow(new PaymentInternalException("내부 오류")).given(paymentService).confirm(any());

        perform().andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_GATEWAY_ERROR"));
    }

    @Test
    void 미정의_결제_오류면_500과_PAYMENT_CONFIRM_FAILED를_반환한다() throws Exception {
        willThrow(new PaymentConfirmException("미정의")).given(paymentService).confirm(any());

        perform().andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_CONFIRM_FAILED"));
    }

    @Test
    void 결과가_불명확하면_504와_PAYMENT_RESULT_UNKNOWN을_반환한다() throws Exception {
        willThrow(new PaymentResultUnknownException("확인 필요")).given(paymentService).confirm(any());

        perform().andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_RESULT_UNKNOWN"));
    }
}
