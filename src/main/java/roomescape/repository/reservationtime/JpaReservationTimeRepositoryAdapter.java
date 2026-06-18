package roomescape.repository.reservationtime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaEntity;
import roomescape.repository.reservationtime.jpa.ReservationTimeJpaRepository;

@Repository
@Primary
public class JpaReservationTimeRepositoryAdapter implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    public JpaReservationTimeRepositoryAdapter(
            final ReservationTimeJpaRepository reservationTimeJpaRepository
    ) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll().stream()
                .map(ReservationTimeJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<ReservationTime> findById(final long timeId) {
        return reservationTimeJpaRepository.findById(timeId)
                .map(ReservationTimeJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public int deleteById(final long timeId) {
        if (!reservationTimeJpaRepository.existsById(timeId)) {
            return 0;
        }

        try {
            reservationTimeJpaRepository.deleteById(timeId);
            reservationTimeJpaRepository.flush();
            return 1;
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    @Transactional
    public ReservationTime save(final ReservationTime reservationTime) {
        try {
            ReservationTimeJpaEntity saved = reservationTimeJpaRepository.saveAndFlush(
                    ReservationTimeJpaEntity.from(reservationTime)
            );
            return saved.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new PersistenceConflictException(exception);
        }
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }
}
