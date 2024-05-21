package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndTimeIdAndMemberId(LocalDate date, Long timeId, Long memberId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            """)
    List<Reservation> findAll();

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member = :member
            """)
    List<Reservation> findByMember(Member member);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member.id = :memberId AND r.theme.id = :themeId
                  AND r.date >= :dateFrom AND r.date <= :dateTo
            """)
    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                LocalDate dateTo);
}
