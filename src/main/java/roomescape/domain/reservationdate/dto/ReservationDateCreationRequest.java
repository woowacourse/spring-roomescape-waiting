package roomescape.domain.reservationdate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.domain.reservationdate.ReservationDate;

public record ReservationDateCreationRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "플레이 날짜는 필수입니다")
    LocalDate playDay
) {

    public ReservationDate toEntity() {
        return ReservationDate.createWithoutId(playDay);
    }
}
