package roomescape.reservation.domain.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long>, JpaSpecificationExecutor<MemberReservation> {

    default MemberReservation getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_RESERVATION_NOT_FOUND,
                        String.format("회원 예약(MemberReservation) 정보가 존재하지 않습니다. [memberReservationId: %d]", id)));
    }

    default List<MemberReservation> searchWith(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return this.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (memberId != null) {
                predicates.add(cb.equal(root.get("member").get("id"), memberId));
            }
            if (themeId != null) {
                predicates.add(cb.equal(root.get("reservationDetail").get("theme").get("id"), themeId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reservationDetail").get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reservationDetail").get("date"), dateTo));
            }
            predicates.add(cb.equal(root.get("status"), ReservationStatus.RESERVED));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    // TODO: 지나지 않은 날짜의 예약만 조회 가능하므로, 지난 예약 보기 기능을 따로 추가하기
    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservationDetail r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.member = :member AND r.date > CURRENT_DATE OR (r.date = CURRENT_DATE AND rt.startAt >= CURRENT_TIME)
            ORDER BY mr.id ASC
            """)
    List<MemberReservation> findNotOverdueByMemberOrderByIdAsc(final Member member);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN mr.reservationDetail r
            WHERE mr.member = :member AND r = :detail
            """)
    Optional<MemberReservation> findByMemberAndDetail(Member member, ReservationDetail detail);

    Long countByReservationDetail(ReservationDetail reservationDetail);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservationDetail r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.status = :status
            """)
    List<MemberReservation> findByStatus(ReservationStatus status);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN FETCH mr.reservationDetail r JOIN FETCH r.reservationTime rt JOIN FETCH r.theme t
            WHERE mr.status = :status
            GROUP BY mr.id
            HAVING mr.id = MIN(mr.id)
            """)
    List<MemberReservation> findFirstOrderByStatus(ReservationStatus status);

    @Query("""
            SELECT mr
            FROM MemberReservation mr JOIN mr.reservationDetail r
            WHERE r = :reservationDetail AND mr.status = WAITING
            ORDER BY mr.id ASC
            """)
    List<MemberReservation> findFirstOrderWaitingByDetail(ReservationDetail reservationDetail, Pageable pageable);
}
