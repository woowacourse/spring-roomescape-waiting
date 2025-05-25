package roomescape.repository.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationitem.ReservationTime;
import roomescape.domain.reservationitem.ReservationTimeRepository;
import roomescape.repository.jpa.ReservationTimeJpaRepository;

@RequiredArgsConstructor
@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Override
    public Optional<ReservationTime> findById(final Long id) {
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTime> findAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public void deleteById(final long id) {
        reservationTimeJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeJpaRepository.existsByStartAt(startAt);
    }
}
