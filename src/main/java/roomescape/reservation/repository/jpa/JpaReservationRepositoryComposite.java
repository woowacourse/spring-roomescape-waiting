package roomescape.reservation.repository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

@Repository
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
    public boolean existsByScheduleTimeId(Long id) {
        return jpaReservationRepository.existsByScheduleTimeId(id);
    }

    @Override
    public List<Reservation> findByMemberAndThemeAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return jpaReservationRepository.findByMemberAndThemeAndDateBetween(
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
    public boolean existsByMemberAndSchedule(Member member, Schedule schedule) {
        return jpaReservationRepository.existsByMemberAndSchedule(member, schedule);
    }

    @Override
    public boolean existsBySchedule(Schedule schedule) {
        return jpaReservationRepository.existsBySchedule(schedule);
    }
}
