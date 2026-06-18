package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.repository.entity.ReservationEntity;

@Profile("jpa")
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private static final String UNIQUE_CONSTRAINT_NAME = "unique_reservation_date_time_theme";

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
            .map(ReservationEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Reservation> findById(final Long reservationId) {
        return reservationJpaRepository.findById(reservationId).map(ReservationEntity::toDomain);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        try {
            ReservationEntity saved = reservationJpaRepository.saveAndFlush(ReservationEntity.from(reservation));
            return Reservation.of(saved.getId(), reservation.getCustomerName(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme());
        } catch (DataIntegrityViolationException e) {
            throw translateViolation(e);
        }
    }

    @Override
    public boolean update(final Reservation reservation) {
        Optional<ReservationEntity> optEntity = reservationJpaRepository.findById(reservation.getId());
        if (optEntity.isEmpty()) {
            return false;
        }
        try {
            optEntity.get().updateSchedule(reservation.getDate(), reservation.getTimeId());
            reservationJpaRepository.saveAndFlush(optEntity.get());
        } catch (DataIntegrityViolationException e) {
            throw translateViolation(e);
        }
        return true;
    }

    @Override
    public boolean deleteById(final Long reservationId) {
        if (!reservationJpaRepository.existsById(reservationId)) {
            return false;
        }
        reservationJpaRepository.deleteById(reservationId);
        return true;
    }

    @Override
    public List<ReservationTimesWithStatus> findReservationTimeStatusesByDateAndThemeId(
        final LocalDate date,
        final Long themeId
    ) {
        return reservationJpaRepository.findTimeStatuses(date, themeId).stream()
            .map(p -> new ReservationTimesWithStatus(p.getId(), p.getStartAt(), p.getReserved()))
            .toList();
    }

    @Override
    public List<Reservation> findAllByCustomerNameAndReservationDateTimeAfter(
        final CustomerName customerName,
        final LocalDateTime now
    ) {
        return reservationJpaRepository
            .findByCustomerNameAfter(customerName.name(), now.toLocalDate(), now.toLocalTime())
            .stream()
            .map(ReservationEntity::toDomain)
            .toList();
    }

    @Override
    public boolean existsBySlot(final LocalDate date, final long reservationTimeId, final long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, reservationTimeId, themeId);
    }

    @Override
    public Optional<Reservation> findBySlot(final LocalDate date, final long timeId, final long themeId) {
        return reservationJpaRepository.findBySlotForUpdate(date, timeId, themeId)
            .map(ReservationEntity::toDomain);
    }

    private static RuntimeException translateViolation(final DataIntegrityViolationException e) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains(UNIQUE_CONSTRAINT_NAME)) {
            return new ReservationAlreadyExistsException();
        }
        return new ReservationOptionChangedException(e);
    }
}
