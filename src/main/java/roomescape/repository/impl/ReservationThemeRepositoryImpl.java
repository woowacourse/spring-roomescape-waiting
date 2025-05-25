package roomescape.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationitem.ReservationTheme;
import roomescape.domain.reservationitem.ReservationThemeRepository;
import roomescape.repository.jpa.ReservationThemeJpaRepository;

@RequiredArgsConstructor
@Repository
public class ReservationThemeRepositoryImpl implements ReservationThemeRepository {

    private final ReservationThemeJpaRepository reservationThemeJpaRepository;

    @Override
    public Optional<ReservationTheme> findById(final Long id) {
       return reservationThemeJpaRepository.findById(id);
    }

    @Override
    public List<ReservationTheme> findAll() {
        return reservationThemeJpaRepository.findAll();
    }

    @Override
    public List<ReservationTheme> findWeeklyThemeOrderByCountDesc(int amount, LocalDate dateFrom, LocalDate dateTo) {
        return reservationThemeJpaRepository.findPopularThemesByRankAndDuration(amount, dateFrom, dateTo);
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
