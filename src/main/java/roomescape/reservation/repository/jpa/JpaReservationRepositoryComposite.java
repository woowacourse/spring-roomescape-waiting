package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
@AllArgsConstructor
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaReservationRepositoryComposite implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

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
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        return jpaReservationRepository.findByMemberAndThemeAndVisitDateBetween(
            themeId,
            memberId,
            dateFrom,
            dateTo
        );
    }

    @Override
    public List<Reservation> findAllByMember(Member member) {
        return jpaReservationRepository.findAllByMember(member);
    }

    @Override
    public Optional<Reservation> findByLowestPriorityByDateAndTimeAndTheme(
        LocalDate date,
        ReservationTime time,
        Theme theme
    ) {
        return jpaReservationRepository.findByLowestPriorityByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public long findWaitingOrder(Reservation reservation) {
        return jpaReservationRepository.findOrder(
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            reservation.getPriority()
        );
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public void delete(Reservation reservation) {
        jpaReservationRepository.delete(reservation);
    }

    @Override
    public List<Reservation> findHighestPriorityWaitings() {
        return jpaReservationRepository.findHighestPriorityReservations();
    }

    @Override
    public boolean isHighestPriorityWaiting(Reservation reservation) {
        return jpaReservationRepository.isHighestPriorityWaiting(reservation);
    }
}
