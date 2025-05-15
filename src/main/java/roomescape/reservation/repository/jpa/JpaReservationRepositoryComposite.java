package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaReservationRepositoryComposite implements ReservationRepository {
    private final JpaReservationRepository jpaReservationRepository;

    public JpaReservationRepositoryComposite(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
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
    public List<Reservation> findByMemberAndThemeAndVisitDateBetween(
        Long themeId,
        Long memberId,
        String dateFrom,
        String dateTo
    ) {
        return jpaReservationRepository.findByMemberAndThemeAndVisitDateBetween(
            themeId,
            memberId,
            LocalDate.parse(dateFrom),
            LocalDate.parse(dateTo)
        );
    }
}
