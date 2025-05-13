package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

public class ReservationResponse {

    public record ReservationCreateResponse(
            Long id,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        public static ReservationCreateResponse from(Reservation reservation, Theme theme) {
            return new ReservationCreateResponse(
                    reservation.getId(),
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

    public record ReservationReadMemberResponse(
            Long reservationId,
            String theme,
            LocalDate date,
            LocalTime time,
            String status
    ) {
        public static ReservationReadMemberResponse from(Reservation reservation) {
            return new ReservationReadMemberResponse(
                    reservation.getId(),
                    reservation.getTheme().getName(),
                    reservation.getDate(),
                    reservation.getTime().getStartAt(),
                    // TODO: 3단계에서 구현
                    "예약"
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
}
