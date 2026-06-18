package roomescape.repository.reservationslot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.theme.Theme;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaEntity;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaRepository;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaEntity;
import roomescape.repository.reservationslot.jpa.ReservationSlotJpaRepository;
import roomescape.repository.theme.jpa.ThemeJpaEntity;
import roomescape.repository.theme.jpa.ThemeJpaRepository;

@Repository
@Primary
public class JpaReservationSlotRepositoryAdapter implements ReservationSlotRepository {

    private final ReservationSlotJpaRepository reservationSlotJpaRepository;
    private final ThemeJpaRepository themeJpaRepository;
    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    public JpaReservationSlotRepositoryAdapter(
            final ReservationSlotJpaRepository reservationSlotJpaRepository,
            final ThemeJpaRepository themeJpaRepository,
            final ReservationTimeJpaRepository reservationTimeJpaRepository
    ) {
        this.reservationSlotJpaRepository = reservationSlotJpaRepository;
        this.themeJpaRepository = themeJpaRepository;
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
    }

    @Override
    public List<ReservationSlot> findAll() {
        return reservationSlotJpaRepository.findAll().stream()
                .map(ReservationSlotJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<ReservationSlot> findById(final long slotId) {
        return reservationSlotJpaRepository.findById(slotId)
                .map(ReservationSlotJpaEntity::toDomain);
    }

    @Override
    public Optional<ReservationSlot> findBySlot(final ReservationSlot reservationSlot) {
        return reservationSlotJpaRepository.findByDateAndTheme_IdAndTime_Id(
                        reservationSlot.getDate(),
                        reservationSlot.getTheme().getId(),
                        reservationSlot.getTime().getId()
                )
                .map(ReservationSlotJpaEntity::toDomain);
    }

    @Override
    public List<ReservationSlot> findByDateAndTheme(final LocalDate date, final Theme theme) {
        return reservationSlotJpaRepository.findAllByDateAndTheme_Id(date, theme.getId()).stream()
                .map(ReservationSlotJpaEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public ReservationSlot save(final ReservationSlot reservationSlot) {
        ThemeJpaEntity theme = themeJpaRepository.getReferenceById(reservationSlot.getTheme().getId());
        ReservationTimeJpaEntity time = reservationTimeJpaRepository.getReferenceById(
                reservationSlot.getTime().getId()
        );

        try {
            ReservationSlotJpaEntity saved = reservationSlotJpaRepository.saveAndFlush(
                    ReservationSlotJpaEntity.from(reservationSlot, theme, time)
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }
}
