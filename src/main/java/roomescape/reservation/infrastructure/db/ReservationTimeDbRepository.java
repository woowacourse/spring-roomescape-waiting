package roomescape.reservation.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.infrastructure.db.dao.ReservationTimeJpaRepository;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationTimeRepository;

@Repository
@RequiredArgsConstructor
public class ReservationTimeDbRepository implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeJpaRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> getAll() {
        return reservationTimeJpaRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeJpaRepository.findById(id);
    }

    @Override
    public ReservationTime getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 시간이 존재하지 않습니다."));
    }

    @Override
    public void remove(ReservationTime reservationTime) {
        reservationTimeJpaRepository.delete(reservationTime);
    }

    @Override
    public List<ReservationTime> getAllByThemeIdAndDate(Long themeId, LocalDate date) {
        return reservationTimeJpaRepository.findByThemeIdAndDate(themeId, date);
    }
}
