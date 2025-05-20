package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationThemeRepository;
import roomescape.repository.jpa.ReservationThemeJpaRepository;

@Repository
public class ReservationThemeRepositoryImpl implements ReservationThemeRepository {

    private final ReservationThemeJpaRepository reservationThemeJpaRepository;

    public ReservationThemeRepositoryImpl(final ReservationThemeJpaRepository reservationThemeJpaRepository) {
        this.reservationThemeJpaRepository = reservationThemeJpaRepository;
    }

    @Override
    public Optional<ReservationTheme> findById(final Long id) {
       return reservationThemeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTheme> findAll() {
        return reservationThemeJpaRepository.findAll();
    }

    @Override
    public List<ReservationTheme> findWeeklyThemeOrderByCountDesc() {
        return reservationThemeJpaRepository.findPopularThemesByRankAndDuration(
                10,
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(1)
        );
    }

    @Override
    public ReservationTheme save(final ReservationTheme reservationTheme) {
        return reservationThemeJpaRepository.save(reservationTheme);
    }

    @Override
    public void deleteById(final long id) {
        reservationThemeJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(final String name) {
        return reservationThemeJpaRepository.existsByName(name);
    }
}
