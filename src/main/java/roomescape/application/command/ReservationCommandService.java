package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.repository.ReservationRepository;

@Service
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

    @Transactional
    public Reservation save(Member reserver, Slot slot) {
        Reservation reservation = Reservation.createWith(
                reserver,
                slot,
                now()
        );

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateMine(Reservation existing, Member requester, Slot targetSlot) {
        Reservation updated = existing.updateWith(
                requester,
                targetSlot,
                now()
        );

        return reservationRepository.update(updated);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteMine(Reservation reservation, Member requester) {
        reservation.cancelBy(
                requester,
                now()
        );

        reservationRepository.deleteById(reservation.getId());
    }

    @Transactional
    public Reservation changeReserver(Reservation reservation, Member requester, Member newReserver) {
        reservation.cancelBy(
                requester,
                now()
        );

        Reservation changed = reservation.changeReserverTo(
                newReserver
        );

        return reservationRepository.updateReserver(changed);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
