package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.MyReservation;

public record MyReservationResponse(
        String resourceKey,
        Long id,
        String name,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        String status,
        @JsonInclude(Include.NON_NULL)
        Long waitingNumber,
        @JsonInclude(Include.NON_NULL)
        String paymentStatus,
        @JsonInclude(Include.NON_NULL)
        String orderId,
        @JsonInclude(Include.NON_NULL)
        String paymentKey,
        @JsonInclude(Include.NON_NULL)
        Long amount
) {
    public static MyReservationResponse from(MyReservation myReservation) {
        return new MyReservationResponse(
                myReservation.getResourceType() + ":" + myReservation.getId(),
                myReservation.getId(),
                myReservation.getName(),
                myReservation.getThemeName(),
                myReservation.getDate(),
                myReservation.getStartAt(),
                myReservation.getStatus(),
                myReservation.getWaitingNumber(),
                myReservation.getPaymentStatus(),
                myReservation.getOrderId(),
                myReservation.getPaymentKey(),
                myReservation.getAmount()
        );
    }
}
