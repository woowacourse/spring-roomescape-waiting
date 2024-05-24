package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWait;

public interface ReservationWaitRepository extends Repository<ReservationWait, Long> {

    ReservationWait save(ReservationWait wait);

    List<ReservationWait> findAll();

    List<ReservationWait> findAllByMemberAndStatus(Member member, ReservationStatus status);

    @Query("""
            SELECT w
            FROM ReservationWait w
            JOIN w.member m
            JOIN w.reservation r
            WHERE r.date >= :start AND r.date <= :end
            AND m.name = :memberName
            AND w.status = :status
            AND r.theme.name = :themeName
            """)
    List<ReservationWait> findByPeriodAndMemberAndThemeAndStatus(@Param("start") LocalDate start,
                                                                 @Param("end") LocalDate end,
                                                                 @Param("memberName") String memberName,
                                                                 @Param("themeName") String themeName,
                                                                 @Param("status") ReservationStatus status);

    List<ReservationWait> findByMemberIdAndStatus(Long memberId, ReservationStatus status);

    @Query("""
            SELECT w.priority
            FROM ReservationWait w
            ORDER BY w.priority DESC
            LIMIT 1
            """)
    Optional<Long> findPriorityIndex();

    List<ReservationWait> findByMemberAndReservation(Member member, Reservation reservation);

    List<ReservationWait> findAllByMember(Member member);

    long countByPriorityBefore(long priority);

    void deleteById(Long id);

    void deleteByMemberId(Long memberId);

    void deleteByMemberAndReservation(Member member, Reservation reservation);

    void deleteByReservationId(Long reservationId);

    void deleteAll();
}
