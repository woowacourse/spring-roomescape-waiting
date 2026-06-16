package roomescape.payment.dto;

import roomescape.domain.reservation.dto.ReservationResponse;

public record PaymentConfirmResult(PaymentResult paymentResult, ReservationResponse reservation) {}
