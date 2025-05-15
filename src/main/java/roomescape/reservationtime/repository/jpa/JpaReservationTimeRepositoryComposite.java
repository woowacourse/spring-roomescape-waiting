package roomescape.reservationtime.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
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
