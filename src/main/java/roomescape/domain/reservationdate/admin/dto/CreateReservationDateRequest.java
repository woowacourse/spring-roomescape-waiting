package roomescape.domain.reservationdate.admin.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

public record CreateReservationDateRequest(
    @NotNull(message = "예약 날짜는 필수 사항 입니다. 날짜를 선택해주세요.")
    LocalDate reservationDate
) {

    public ReservationDate toEntity() {
        return ReservationDate.createWithoutId(reservationDate);
    }
}
