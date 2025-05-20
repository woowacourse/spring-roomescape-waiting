package roomescape.reservation.infrastructure.db;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.infrastructure.db.dao.ReservationThemeJpaRepository;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.repository.ReservationThemeRepository;

@Repository
@RequiredArgsConstructor
public class ReservationThemeDbRepository implements ReservationThemeRepository {

    private final ReservationThemeJpaRepository reservationThemeJpaRepository;

    @Override
    public ReservationTheme save(ReservationTheme reservationTheme) {
        return reservationThemeJpaRepository.save(reservationTheme);
    }

    @Override
    public List<ReservationTheme> getAll() {
        return reservationThemeJpaRepository.findAll();
    }

    @Override
    public void remove(ReservationTheme reservation) {
        reservationThemeJpaRepository.delete(reservation);
    }

    @Override
    public Optional<ReservationTheme> findById(Long id) {
        return reservationThemeJpaRepository.findById(id);
    }

    @Override
    public ReservationTheme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 테마가 존재하지 않습니다."));
    }

    @Override
    public List<ReservationTheme> getPopularThemesWithLimit(int limit) {
        return reservationThemeJpaRepository.getOrderByThemeBookedCountWithLimit(limit);
    }
}
