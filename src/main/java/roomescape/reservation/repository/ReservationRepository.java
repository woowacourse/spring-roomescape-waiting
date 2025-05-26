package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    void deleteById(Long id);

    boolean existsByTimeId(Long id);

    List<Reservation> findByMemberAndThemeAndVisitDateBetween(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);

    Optional<Reservation> findByLastPriorityByDateAndTimeAndTheme(
        LocalDate date,
        ReservationTime time,
        Theme theme
    );

    long findOrder(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void delete(Reservation reservation);

    List<Reservation> findHighestPriorityWaitings();

    boolean isHighestPriorityWaiting(Reservation reservation);
}
