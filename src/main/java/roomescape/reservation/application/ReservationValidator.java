package roomescape.reservation.application;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.time.domain.ReservationTime;

@Component
public class ReservationValidator {

    private final ReservationRepository reservationRepository;

    public ReservationValidator(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public void validateReservationExists(Long reservationId) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new IllegalStateException("이미 삭제되어 있는 리소스입니다.");
        }
    }

    public void validateUserDeletion(Reservation reservation, Long memberId) {
        if (!Objects.equals(reservation.getMember().getId(), memberId)) {
            throw new IllegalStateException("다른 사람의 예약을 삭제할 수 없습니다.");
        }
        if (reservation.getStatus().equals(ReservationStatus.RESERVED)) {
            throw new IllegalStateException("이미 예약된 상태 내역은 삭제할 수 없습니다.");
        }
    }

    public void validateReservationDateTime(LocalDate date, ReservationTime reservationTime) {
        LocalDateTime dateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        validateIsPast(dateTime);
        validateIsDuplicate(date, reservationTime);
    }

    private void validateIsPast(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new DateTimeException("지난 일시에 대한 예약 생성은 불가능합니다.");
        }
    }

    private void validateIsDuplicate(LocalDate date, ReservationTime reservationTime) {
        if (reservationRepository.existsByDateAndReservationTimeStartAt(date, reservationTime.getStartAt())) {
            throw new IllegalStateException("중복된 일시의 예약은 불가능합니다.");
        }
    }
}
