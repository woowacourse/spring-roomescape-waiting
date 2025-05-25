package roomescape.reservation.repository;

import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    boolean existsByScheduleTimeId(Long id);

    List<Reservation> findByMemberAndThemeAndVisitDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);

    boolean existsByMemberAndSchedule(Member member, Schedule schedule);

    boolean existsBySchedule(Schedule schedule);
}
