package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithRank;
import roomescape.domain.RoomTheme;
import roomescape.domain.Status;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    default List<Reservation> findAllReservations() {
        return findByStatusIn(List.of(Status.CREATED));
    }

    default Optional<Reservation> findReservationById(Long id) {
        return findByIdAndStatus(id, Status.CREATED);
    }

    default Optional<Reservation> findWaitingReservationById(Long id) {
        return findByIdAndStatus(id, Status.WAITING);
    }

    default Optional<Reservation> findLatestWaitingReservation() {
        return findTopByStatusInOrderByCreatedAt(List.of(Status.WAITING));
    }

    default List<ReservationWithRank> findMyReservationWithRank(Long id) {
        return findMyReservationsWithRank(id, List.of(Status.CREATED, Status.WAITING));
    }

    default List<Reservation> findAllWaitings() {
        return findByStatusIn(List.of(Status.WAITING));
    }

    default List<Reservation> findBy(Specification<Reservation> specification) {
        return findAll(specification);
    }

    default Optional<Reservation> findMemberReservation(
            LocalDate date,
            ReservationTime time,
            RoomTheme theme,
            Member member) {
        return findByDateAndTimeAndThemeAndMemberAndStatusIn(date, time, theme, member, List.of(Status.CREATED));
    }

    Optional<List<Reservation>> findByThemeIdAndStatusIn(Long themeId, List<Status> statuses);

    Optional<Reservation> findByDateAndTimeAndThemeAndMemberAndStatusIn(
            LocalDate date,
            ReservationTime time,
            RoomTheme theme,
            Member member,
            List<Status> statuses);

    @Query("""
        SELECT new roomescape.domain.ReservationWithRank(
            r1, CAST((SELECT COUNT(r2) FROM Reservation r2
                     where r2.theme = r1.theme
                     AND r2.date = r1.date
                     AND r2.time = r1.time
                     AND r2.createdAt < r1.createdAt
                     AND r2.status IN :statuses) AS Long))
        FROM Reservation r1
        WHERE r1.member.id = :memberId
        order by r1.date
        """)
    List<ReservationWithRank> findMyReservationsWithRank(Long memberId, List<Status> statuses);

    Optional<Reservation> findByIdAndStatus(Long id, Status status);

    List<Reservation> findByStatusIn(List<Status> statuses);

    Optional<Reservation> findTopByStatusInOrderByCreatedAt(List<Status> waiting);
}
