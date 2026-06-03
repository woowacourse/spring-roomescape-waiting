package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class WaitlistWriter {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;

    public WaitlistWriter(ReservationRepository reservationRepository, WaitlistRepository waitlistRepository) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationWithStatus save(Reservation reservation, LocalDateTime createdAt) {
        verifyNoDuplicateReservation(reservation);

        Long savedId = waitlistRepository.save(reservation, createdAt);
        Waitlist waitlist = waitlistRepository.getById(savedId, "존재하지 않는 예약 대기입니다.");

        return ReservationWithStatus.waiting(waitlist);
    }

    private void verifyNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 예약이 존재합니다.");
        }
        if (waitlistRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "같은 슬롯에 중복 대기가 존재합니다.");
        }
    }
}
