package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Repository
public class ReservationJpaRepository implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationJpaRepository(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationRepository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                             LocalDate dateTo) {
        return jpaReservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, dateFrom, dateTo);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return jpaReservationRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean hasSameReservation(Reservation reservation) {
        return jpaReservationRepository.existsByDateAndTimeStartAtAndThemeId(reservation.getDate(),
                reservation.reservationTime(), reservation.themeId());
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return jpaReservationRepository.existsByThemeId(themeId);
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return jpaReservationRepository.findByMemberId(memberId);
    }
}
