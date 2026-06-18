package roomescape.reservationtime.repository.jpa;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeInUseException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.repository.entity.ReservationTimeEntity;

@Profile("jpa")
@Repository
@RequiredArgsConstructor
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll().stream()
            .map(ReservationTimeEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<ReservationTime> findById(final Long timeId) {
        return reservationTimeJpaRepository.findById(timeId).map(ReservationTimeEntity::toDomain);
    }

    @Override
    public ReservationTime save(final ReservationTime newReservationTime) {
        return reservationTimeJpaRepository.saveAndFlush(
            ReservationTimeEntity.from(newReservationTime)
        ).toDomain();
    }

    @Override
    public boolean delete(final Long timeId) {
        if (!reservationTimeJpaRepository.existsById(timeId)) {
            return false;
        }
        try {
            reservationTimeJpaRepository.deleteById(timeId);
            reservationTimeJpaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ReservationTimeInUseException(e);
        }
        return true;
    }
}
