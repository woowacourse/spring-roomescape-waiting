package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

public class ReservationResponse {

    public record ReservationCreateResponse(
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        public static ReservationCreateResponse from(Reservation reservation, Theme theme) {
            return new ReservationCreateResponse(
                    reservation.getDate(),
                    reservation.getTime(),
                    theme
            );
        }
    }

    public record ReservationReadResponse(
            Long id,
            LocalDate date,
            ReservationTime time,
            Member member,
            Theme theme
    ) {
        public static ReservationReadResponse from(Reservation reservation, Member member, Theme theme) {
            return new ReservationReadResponse(
                    reservation.getId(),
                    reservation.getDate(),
                    reservation.getTime(),
                    member,
                    theme
            );
        }
    }

    public record ReservationAdminCreateResponse(
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {

        public static ReservationAdminCreateResponse from(Reservation reservation, Theme theme) {
            return new ReservationAdminCreateResponse(
                    reservation.getDate(),
                    reservation.getTime(),
                    theme
            );
        }
    }

    public record ReservationReadFilteredResponse(
            Long id,
            LocalDate date,
            ReservationTime time,
            Member member,
            Theme theme
    ) {
        public static ReservationReadFilteredResponse from(Reservation reservation, Member member, Theme theme) {
            return new ReservationReadFilteredResponse(
                    reservation.getId(),
                    reservation.getDate(),
                    reservation.getTime(),
                    member,
                    theme
            );
        }
    }
}
