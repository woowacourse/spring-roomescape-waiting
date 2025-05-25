package roomescape.reservation.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.domain.DomainTerm;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.user.domain.UserId;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final TimeProvider timeProvider;

    public Reservation create(final Reservation reservation) {
        reservation.validatePast(timeProvider.now());

        final ReservationSlot slot = reservation.getSlot();
        final UserId userId = reservation.getUserId();

        // TODO 중복 검증 해결
        if (reservationQueryService.existsBySlotAndUserId(slot, userId)) {
            throw new DuplicateException(DomainTerm.RESERVATION, slot, userId);
        }

        if (!reservationQueryService.existsBySlot(slot)) {
            reservation.approved();
        }

        return reservationRepository.save(reservation);
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
