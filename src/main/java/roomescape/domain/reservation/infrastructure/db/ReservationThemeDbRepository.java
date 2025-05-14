package roomescape.domain.reservation.infrastructure.db;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.infrastructure.db.dao.ReservationThemeDao;
import roomescape.domain.reservation.model.entity.ReservationTheme;
import roomescape.domain.reservation.model.repository.ReservationThemeRepository;
import roomescape.global.exception.ResourceInUseException;
import roomescape.global.exception.ResourceNotFoundException;

@Repository
@RequiredArgsConstructor
public class ReservationThemeDbRepository implements ReservationThemeRepository {

    private final ReservationThemeDao reservationThemeDao;

    @Override
    public List<ReservationTheme> getAll() {
        return reservationThemeDao.selectAll();
    }

    @Override
    public ReservationTheme save(ReservationTheme reservationTime) {
        return reservationThemeDao.insertAndGet(reservationTime);
    }

    @Override
    public Optional<ReservationTheme> findById(Long id) {
        return reservationThemeDao.selectById(id);
    }

    @Override
    public ReservationTheme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 테마가 존재하지 않습니다."));
    }

    @Override
    public void remove(ReservationTheme reservationTime) {
        try {
            reservationThemeDao.deleteById(reservationTime.getId());
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하려는 테마를 가진 예약이 존재합니다.", e);
        }
    }

    @Override
    public List<ReservationTheme> getPopularThemesWithLimit(int limit) {
        return reservationThemeDao.getOrderByThemeBookedCountWithLimit(limit);
    }
}
