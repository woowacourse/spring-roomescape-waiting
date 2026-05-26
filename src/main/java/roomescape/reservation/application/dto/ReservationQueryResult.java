package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationDetail;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.theme.application.dto.ThemeQueryResult;

public record ReservationQueryResult(Long id, String name, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                     ThemeQueryResult theme, ReservationTimeQueryResult time) {

    public static ReservationQueryResult from(Reservation reservation, ThemeQueryResult themeQueryResult,
                                              ReservationTimeQueryResult timeQueryResult) {
        return new ReservationQueryResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                themeQueryResult,
                timeQueryResult
        );
    }

    public static ReservationQueryResult from(ReservationDetail reservationDetail) {
        return new ReservationQueryResult(
                reservationDetail.reservationId(),
                reservationDetail.username(),
                reservationDetail.date(),
                ThemeQueryResult.from(
                        reservationDetail.themeId(),
                        reservationDetail.themeName(),
                        reservationDetail.themeDescription(),
                        reservationDetail.thumbnailImgUrl()
                ),
                ReservationTimeQueryResult.from(
                        reservationDetail.timeId(),
                        reservationDetail.startAt()
                )
        );
    }
}
