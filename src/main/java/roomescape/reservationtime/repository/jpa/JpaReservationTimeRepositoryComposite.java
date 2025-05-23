package roomescape.reservationtime.repository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaReservationTimeRepositoryComposite implements ReservationTimeRepository {
    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    public JpaReservationTimeRepositoryComposite(JpaReservationTimeRepository jpaReservationTimeRepository) {
        this.jpaReservationTimeRepository = jpaReservationTimeRepository;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jpaReservationTimeRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationTimeRepository.deleteById(id);
    }

    @Override
    public List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationTimeRepository.findAllByReservationDateAndThemeId(date, themeId);
    }
}
