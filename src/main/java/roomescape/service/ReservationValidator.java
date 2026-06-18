package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;

@Component
public class ReservationValidator {

    private final JpaReservationRepository reservationRepository;

    public ReservationValidator(JpaReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateNotPast(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION, "이미 지난 시간으로는 예약할 수 없습니다.");
        }
    }

    public void validateNotReserved(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTime_IdAndTheme_Id(date, timeId, themeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다.");
        }
    }

    public void validateUpdatableReservation(Reservation reservation, String name) {
        validateOwner(reservation, name);
        validateReservationNotLocked(reservation);
    }

    public void validateUpdatePolicy(Reservation reservation, Reservation updatedReservation) {
        validateScheduleChanged(reservation, updatedReservation);
        validateNotPast(updatedReservation.getDate(), updatedReservation.getTime());
        validateNotReserved(
                updatedReservation.getDate(),
                updatedReservation.getTime().getId(),
                updatedReservation.getTheme().getId());
    }

    public void validateUpdateValueExists(LocalDate date, Long timeId) {
        if (date == null && timeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "변경할 날짜 또는 시간이 필요합니다.");
        }
    }

    private void validateOwner(Reservation reservation, String name) {
        if (!reservation.isOwnedBy(name)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_RESERVATION, "본인의 예약만 변경하거나 취소할 수 있습니다.");
        }
    }

    private void validateReservationNotLocked(Reservation reservation) {
        if (reservation.isPast()) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_LOCKED, "이미 지난 예약은 변경하거나 취소할 수 없습니다.");
        }
    }

    private void validateScheduleChanged(Reservation reservation, Reservation updatedReservation) {
        if (reservation.hasSameSchedule(updatedReservation)) {
            throw new BusinessException(ErrorCode.UNCHANGED_RESERVATION, "기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다.");
        }
    }
}
