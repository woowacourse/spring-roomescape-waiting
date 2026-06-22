package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.domain.exception.NotFoundException;

@Service
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationCommandService(
            ReservationRepository reservationRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    public Reservation save(Member reserver, Slot slot) {
        Reservation reservation = Reservation.createWith(
                reserver,
                slot,
                now()
        );

        return reservationRepository.save(reservation);
    }

    public void promote(ReservationWaiting waiting) {
        Reservation reservation = Reservation.promoteFrom(
                waiting.getWaiter(),
                waiting.getSlot()
        );

        reservationRepository.save(reservation);
    }

    public Reservation updateMine(Reservation existing, Member requester, Slot targetSlot) {
        Reservation updated = existing.updateWith(
                requester,
                targetSlot,
                now()
        );

        return reservationRepository.update(updated);
    }

    public Reservation getByIdForUpdate(Long id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약입니다. Id: " + id));
    }

    public void delete(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    public void deleteMine(Reservation reservation, Member requester) {
        reservation.validateCancellableBy(
                requester,
                now()
        );

        reservationRepository.deleteById(reservation.getId());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
