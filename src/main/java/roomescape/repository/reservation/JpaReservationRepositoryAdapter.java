package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservation.jpa.ReservationJpaEntity;
import roomescape.repository.reservation.jpa.ReservationJpaRepository;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaRepository;

@Repository
@Primary
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationSlotJpaRepository reservationSlotJpaRepository;

    public JpaReservationRepositoryAdapter(
            final ReservationJpaRepository reservationJpaRepository,
            final ReservationSlotJpaRepository reservationSlotJpaRepository
    ) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.reservationSlotJpaRepository = reservationSlotJpaRepository;
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
                .map(ReservationJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        return reservationJpaRepository.findById(id)
                .map(ReservationJpaEntity::toDomain);
    }

    @Override
    public Optional<Reservation> findBySlot(final ReservationSlot slot) {
        return reservationJpaRepository.findBySlot_DateAndSlot_Theme_IdAndSlot_Time_Id(
                        slot.getDate(),
                        slot.getTheme().getId(),
                        slot.getTime().getId()
                )
                .map(ReservationJpaEntity::toDomain);
    }

    @Override
    public List<Reservation> findByName(ReservationName name) {
        return reservationJpaRepository.findByName(name.value()).stream()
                .map(ReservationJpaEntity::toDomain)
                .toList();
    }


    @Override
    public List<Reservation> findByDateAndTheme(final LocalDate date, final Theme theme) {
        return reservationJpaRepository.findAllBySlot_DateAndSlot_Theme_Id(date, theme.getId()).stream()
                .map(ReservationJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTime(final ReservationTime time) {
        return reservationJpaRepository.existsBySlot_Time_Id(time.getId());
    }

    @Override
    @Transactional
    public Reservation save(final Reservation reservation) {
        ReservationSlotJpaEntity slot = reservationSlotJpaRepository.getReferenceById(
                reservation.getSlot().getId()
        );

        try {
            ReservationJpaEntity saved = reservationJpaRepository.saveAndFlush(
                    ReservationJpaEntity.from(reservation, slot)
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    @Transactional
    public void delete(final Reservation reservation) {
        try {
            reservationJpaRepository.deleteById(reservation.getId());
            reservationJpaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }
}
