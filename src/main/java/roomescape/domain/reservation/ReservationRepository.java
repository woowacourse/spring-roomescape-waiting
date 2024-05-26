package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.detail.ReservationDetail;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    List<Reservation> findByMemberId(long memberId);

    boolean existsByDetail_TimeId(long timeId);

    boolean existsByDetail_ThemeId(long id);

    boolean existsByDetail(ReservationDetail detail);

    boolean existsByDetailAndMemberId(ReservationDetail detail, long memberId);

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.detail
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.detail.theme.id = :themeId)
                AND (:dateFrom IS NULL OR r.detail.date >= :dateFrom)
                AND (:dateTo IS NULL OR r.detail.date <= :dateTo)
            """)
    List<Reservation> findAllByConditions(
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", id)));
    }
}
