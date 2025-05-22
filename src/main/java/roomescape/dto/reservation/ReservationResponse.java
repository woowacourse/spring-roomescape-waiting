package roomescape.dto.reservation;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationResponse(Long id, LocalDate date, ReservationTimeResponse time,
                                  ThemeResponse theme, MemberResponse member, String status) {

    public static ReservationResponse from(Reservation reservation) {
        String statusMessage = getStatusMessage(reservation);
        return new ReservationResponse(reservation.getId(), reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()), ThemeResponse.from(reservation.getTheme()),
                MemberResponse.from(reservation.getMember()),
                statusMessage);
    }

    public static List<ReservationResponse> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private static String getStatusMessage(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        String statusMessage = "예약"; // TODO: View 영역이니까 DTO에 두기 vs Reservation 내부에서 판단하고 주기 (이건 아닌 듯)

        if (status.isWaiting()) {
            statusMessage = String.format("%d번째 예약대기", status.getPriority());
        }
        return statusMessage;
    }
}
