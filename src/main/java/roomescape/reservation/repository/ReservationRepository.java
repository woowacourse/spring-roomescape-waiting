package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

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
}
