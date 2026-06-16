package roomescape.repository.reservationwaiting;

import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservationwaiting.jpa.ReservationWaitingJpaEntity;
import roomescape.repository.reservationwaiting.jpa.ReservationWaitingJpaRepository;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaRepository;

@Repository
@Primary
public class JpaReservationWaitingRepositoryAdapter implements ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;
    private final ReservationSlotJpaRepository reservationSlotJpaRepository;

    public JpaReservationWaitingRepositoryAdapter(
            final ReservationWaitingJpaRepository reservationWaitingJpaRepository,
            final ReservationSlotJpaRepository reservationSlotJpaRepository
    ) {
        this.reservationWaitingJpaRepository = reservationWaitingJpaRepository;
        this.reservationSlotJpaRepository = reservationSlotJpaRepository;
    }

    @Override
    @Transactional
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        ReservationSlotJpaEntity slot = reservationSlotJpaRepository.getReferenceById(
                reservationWaiting.getSlot().getId()
        );

        try {
            ReservationWaitingJpaEntity saved = reservationWaitingJpaRepository.saveAndFlush(
                    ReservationWaitingJpaEntity.from(reservationWaiting, slot)
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    public Optional<ReservationWaiting> findById(final Long id) {
        return reservationWaitingJpaRepository.findById(id)
                .map(ReservationWaitingJpaEntity::toDomain);
    }

    @Override
    public ReservationWaitingLine findLineBySlot(final ReservationSlot slot) {
        return ReservationWaitingLine.fromWaitings(
                reservationWaitingJpaRepository.findAllBySlot_IdOrderByRequestedAtAscIdAsc(slot.getId()).stream()
                        .map(ReservationWaitingJpaEntity::toDomain)
                        .toList()
        );
    }

    @Override
    @Transactional
    public void delete(final ReservationWaiting reservationWaiting) {
        reservationWaitingJpaRepository.deleteById(reservationWaiting.getId());
        reservationWaitingJpaRepository.flush();
    }
}
