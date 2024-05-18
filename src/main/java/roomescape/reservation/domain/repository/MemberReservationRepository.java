package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.dto.MyReservationProjection;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    @Query("""
                SELECT mr
                FROM MemberReservation mr
                JOIN FETCH mr.reservation r
                JOIN FETCH mr.member m
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
                WHERE (:memberId IS NULL OR m.id = :memberId) 
                    AND (:themeId IS NULL OR th.id = :themeId) 
                    AND :startDate <= r.date 
                    AND r.date <= :endDate
            """)
    List<MemberReservation> findBy(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    @Query(value = """
            SELECT RN_TABLE.ID AS id, TH.name as themeName, RE.date as date, T.START_AT as time, RN_TABLE.RN as waitingNumber
            FROM
            (SELECT ID, MEMBER_ID, RESERVATION_ID, ROW_NUMBER() OVER(PARTITION BY RESERVATION_ID ORDER BY CREATED_AT) AS RN
            FROM MEMBER_RESERVATION) AS RN_TABLE
            LEFT JOIN MEMBER AS M ON RN_TABLE.MEMBER_ID = M.ID
            LEFT JOIN RESERVATION AS RE ON RN_TABLE.RESERVATION_ID = RE.ID
            LEFT JOIN RESERVATION_TIME AS T ON RE.TIME_ID = T.ID
            LEFT JOIN THEME AS TH ON RE.THEME_ID = TH.ID
            WHERE MEMBER_ID = ?;
            """, nativeQuery = true)
    List<MyReservationProjection> findByMember(long memberId);


    void deleteByReservationId(long reservationId);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
