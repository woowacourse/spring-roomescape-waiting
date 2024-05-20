package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
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
                    AND mr.reservationStatus = :status
            """)
    List<MemberReservation> findBy(Long memberId, Long themeId, ReservationStatus status, LocalDate startDate,
                                   LocalDate endDate);

    @Query(value = """
            SELECT RN_TABLE.ID AS id, TH.name as themeName, RE.date as date, T.START_AT as time, RN_TABLE.RN as waitingNumber, RESERVATION_STATUS as status
            FROM
            (SELECT ID, MEMBER_ID, RESERVATION_ID, COUNT(*) OVER(PARTITION BY RESERVATION_ID ORDER BY CREATED_AT) AS RN, RESERVATION_STATUS
            FROM MEMBER_RESERVATION) AS RN_TABLE
            LEFT JOIN MEMBER AS M ON RN_TABLE.MEMBER_ID = M.ID
            LEFT JOIN RESERVATION AS RE ON RN_TABLE.RESERVATION_ID = RE.ID
            LEFT JOIN RESERVATION_TIME AS T ON RE.TIME_ID = T.ID
            LEFT JOIN THEME AS TH ON RE.THEME_ID = TH.ID
            WHERE MEMBER_ID = ?;
            """, nativeQuery = true)
    List<MyReservationProjection> findByMember(long memberId);

    List<MemberReservation> findAllByReservationStatus(ReservationStatus reservationStatus);

    @Modifying
    @Query(value = """
            UPDATE MemberReservation mr
            SET mr.reservationStatus = :toSetStatus
            WHERE mr.reservation = :reservation
            AND mr.reservationStatus = :toChangeStatus
            AND mr.id IN (
                SELECT rn_table.id
                FROM (
                    SELECT mr2.id as id, COUNT(*) OVER(PARTITION BY mr2.reservation.id ORDER BY mr2.createdAt) AS rn
                    FROM MemberReservation mr2
                ) rn_table
                WHERE rn_table.rn = :waitingNumber
            )
            """)
    void updateStatusByReservationIdAndWaitingNumber(ReservationStatus toSetStatus, Reservation reservation,
                                                     ReservationStatus toChangeStatus, int waitingNumber);

    void deleteByReservationId(long reservationId);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
