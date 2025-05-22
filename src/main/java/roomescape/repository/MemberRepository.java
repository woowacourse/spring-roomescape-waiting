package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Member;
import roomescape.global.ReservationStatus;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            select m
            from Member m
            left join fetch m.reservations r
            join fetch r.reservationTime
            join fetch r.theme
            where m.id = :memberId
            """)
    Optional<Member> findFetchById(@Param("memberId") Long memberId);

    Optional<Member> findByEmailAndPassword(String email, String password);

    @Query(value = """
            select m
            from Member m
            join Reservation r on r.member.id = m.id
            where r.date = :date
            and r.reservationTime.id = :timeId
            and r.theme.id = :themeId
            and r.status = :status
            """)
    List<Member> findNextReserveMember(@Param("date") LocalDate date,
                                       @Param("timeId") Long timeId,
                                       @Param("themeId") Long themeId,
                                       @Param("status") ReservationStatus status,
                                       Pageable pageable);

    boolean existsByEmail(String email);
}
