package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findByMember(Member member);

    @Query("""
            SELECT r 
                FROM Reservation r 
                WHERE (:themeId IS NULL OR r.theme.id = :themeId) 
                    AND (:memberId IS NULL OR r.member.id = :memberId) 
                    AND (:dateFrom IS NULL OR r.date >= :dateFrom) 
                    AND (:dateTo IS NULL OR r.date <= :dateTo)
            """)
    List<Reservation> findByMemberAndThemeAndDateRange(
            @Param("memberId") Long member,
            @Param("themeId") Long theme,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    void deleteById(Long id);
}
