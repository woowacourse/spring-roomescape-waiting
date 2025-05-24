package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            select r from Reservation r
            join RoomEscapeInformation re
            on r.roomEscapeInformation.id = re.id
            where (:themeId is null or re.theme.id = :themeId)
              and (:memberId is null or r.member.id = :memberId)
              and (:localDateFrom is null or re.date >= :localDateFrom)
              and (:localDateTo is null or re.date <= :localDateTo)
            """)
    List<Reservation> findByCriteria(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("localDateFrom") LocalDate localDateFrom,
            @Param("localDateTo") LocalDate localDateTo
    );

    List<Reservation> findByMember(final Member member);

    boolean existsByRoomEscapeInformationId(Long roomEscapeInformationId);
}
