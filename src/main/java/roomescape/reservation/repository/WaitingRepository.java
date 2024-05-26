package roomescape.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.model.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    @Query("SELECT new roomescape.reservation.model.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.reservation.id = w.reservation.id " +
            "       AND w2.id < w.id) + 1) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRank(Long memberId);

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    boolean existsByReservationId(Long reservationId);

    @Query("SELECT w.member FROM Waiting w WHERE w.reservation.id = :reservationId ORDER BY w.id ASC LIMIT 1")
    Member findFirstMemberByReservationIdOrderByIdAsc(Long reservationId);

    void deleteByMemberAndReservation(Member firstCandidate, Reservation reservation);
}
