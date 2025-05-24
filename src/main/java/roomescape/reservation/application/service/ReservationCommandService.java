package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.SortedReservationsOfSlot;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final TimeProvider timeProvider;

    public Reservation create(Reservation reservation) {
        reservation.validatePast(timeProvider.now());

        reservation = reservationRepository.save(reservation);

        final ReservationSlot slot = ReservationSlot.from(reservation);
        final SortedReservationsOfSlot reservations = SortedReservationsOfSlot.of(
                slot,
                reservationQueryService.getByReservationSlotAndCreatedAt(
                        slot,
                        reservation.getCreatedAt()));

        if (reservations.isEmpty()) {
            reservation.approved();
        }

        return reservation;
    }

    public void delete(final ReservationId id) {
        if (reservationRepository.existsByParams(id)) {
            reservationRepository.deleteById(id);
            return;
        }

        throw new NotFoundException(DomainTerm.RESERVATION, id);
    }

    public void delete(final Reservation target) {
        reservationRepository.delete(target);
    }
}
