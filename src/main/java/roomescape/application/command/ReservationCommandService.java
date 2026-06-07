package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Slot;
import roomescape.repository.ReservationRepository;

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

    public void delete(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
    }

    public void deleteMine(Reservation reservation, Member requester) {
        reservation.cancelBy(
                requester,
                now()
        );

        reservationRepository.deleteById(reservation.getId());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
