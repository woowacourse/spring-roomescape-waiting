package roomescape.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.dto.ReservationWithMember;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>,
    JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByThemeId(Long reservationThemeId);

    boolean existsByTimeId(Long reservationTimeId);

    // TODO : QueryDSL로 로직 개선하기
    @Query("""
        SELECT NEW roomescape.reservation.model.repository.dto.ReservationWithMember(
              r.id,
              r.date, 
              rt.id , 
              rt.startAt, 
              th.id, 
              th.name, 
              th.description, 
              th.thumbnail, 
              m.id, 
              m.name, 
              m.email)
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme th
        JOIN Member m ON r.memberId = m.id
        """)
    List<ReservationWithMember> findAllWithMember();

    @Query("""
        SELECT NEW roomescape.reservation.model.repository.dto.ReservationWithMember(
              r.id,
              r.date, 
              rt.id , 
              rt.startAt, 
              th.id, 
              th.name, 
              th.description, 
              th.thumbnail, 
              m.id, 
              m.name, 
              m.email)
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme th
        JOIN Member m ON r.memberId = m.id
        WHERE r.id = :reservationId
        """)
    Optional<ReservationWithMember> findWithMemberById(@Param("reservationId") Long reservationId);

    @Query("""
        SELECT NEW roomescape.reservation.model.repository.dto.ReservationWithMember(
              r.id,
              r.date, 
              rt.id , 
              rt.startAt, 
              th.id, 
              th.name, 
              th.description, 
              th.thumbnail, 
              m.id, 
              m.name, 
              m.email)
        FROM Reservation r
        JOIN r.time rt
        JOIN r.theme th
        JOIN Member m ON r.memberId = m.id
        WHERE r.id IN (:reservationIds)
        """)
    List<ReservationWithMember> findAllWithMemberByIds(@Param("reservationIds") List<Long> reservationIds);

    List<Reservation> findAllByMemberId(Long memberId);
}
