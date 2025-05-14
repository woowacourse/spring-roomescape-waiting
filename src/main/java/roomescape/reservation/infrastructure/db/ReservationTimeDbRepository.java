package roomescape.reservation.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.reservation.infrastructure.db.dao.ReservationTimeDao;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.global.exception.ResourceInUseException;
import roomescape.global.exception.ResourceNotFoundException;

@Repository
@RequiredArgsConstructor
public class ReservationTimeDbRepository implements ReservationTimeRepository {

    private final ReservationTimeDao reservationTimeDao;

    @Override
    public List<ReservationTime> getAll() {
        return reservationTimeDao.selectAll();
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeDao.insertAndGet(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimeDao.selectById(id);
    }

    @Override
    public ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 시간이 존재하지 않습니다."));
    }

    @Override
    public void remove(ReservationTime reservationTime) {
        try {
            reservationTimeDao.deleteById(reservationTime.getId());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하려는 시간을 가진 예약이 존재합니다.", e);
        }
    }

    @Override
    public List<ReservationTime> getAllByThemeIdAndDate(Long themeId, LocalDate date) {
        return reservationTimeDao.selectAllByThemeIdAndDate(themeId, date);
    }
}
