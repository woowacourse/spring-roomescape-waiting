package roomescape.payment.dto;

import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

public record PaymentConfirmResult(TossPaymentResponse tossResponse, ReservationResponse reservation) {}
