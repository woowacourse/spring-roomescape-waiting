package roomescape.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryImpl(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public long save(Reservation reservation) {
        return jpaReservationRepository.save(reservation).getId();
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public boolean existsByTimeId(Long id) {
        return jpaReservationRepository.existsByTimeId(id);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationRepository.findAllByDateAndThemeId(date, themeId);
    }

    @Override
    public Optional<Reservation> findById(long addedReservationId) {
        return jpaReservationRepository.findById(addedReservationId);
    }

    @Override
    public List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end) {
        return jpaReservationRepository.findAllByDateBetween(start, end);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(Reservation reservation) {
        return jpaReservationRepository.existsByDateAndTimeAndTheme(reservation.getDate(),
                reservation.getReservationTime(), reservation.getTheme());
    }

    @Override
    public boolean existsByThemeId(long themeId) {
        return jpaReservationRepository.existsByThemeId(themeId);
    }
}
