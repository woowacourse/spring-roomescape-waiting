package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;

public record WaitingReservationResponse(
        Long id,
        WaitingReservationMemberSlot member,
        WaitingReservationThemeSlot theme,
        WaitingReservationTimeSlot time,
        LocalDate date
) {

    public record WaitingReservationMemberSlot(Long memberId, String name) {
    }

    public record WaitingReservationThemeSlot(Long themeId, String themeName) {
    }

    public record WaitingReservationTimeSlot(Long timeId, LocalTime startAt) {
    }

    public static WaitingReservationResponse from(Reservation reservation) {
        return new WaitingReservationResponse(
                reservation.getId(),
                new WaitingReservationMemberSlot(
                        reservation.getMember().getId(),
                        reservation.getMember().getName()
                ),
                new WaitingReservationThemeSlot(
                        reservation.getReservationItem().getTheme().getId(),
                        reservation.getReservationItem().getTheme().getName()
                ),
                new WaitingReservationTimeSlot(
                        reservation.getReservationItem().getTime().getId(),
                        reservation.getReservationItem().getTime().getStartAt()
                ),
                reservation.getReservationItem().getDate()
        );
    }
}
