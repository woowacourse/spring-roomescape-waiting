package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    Optional<List<Reservation>> findByThemeId(Long themeId); //TODO 상태 추가

    Optional<Reservation> findByDateAndTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime time,
            RoomTheme theme,
            Member member);

    @Query("""
        SELECT new roomescape.domain.ReservationWithRank(
            r1, CAST((SELECT COUNT(r2) FROM Reservation r2
                     where r2.theme = r1.theme
                     AND r2.date = r1.date
                     AND r2.time = r1.time
                     AND r2.createdAt < r1.createdAt
                     AND (r2.status = 'WAITING' or r2.status = 'CREATED')) AS Long))
        FROM Reservation r1
        WHERE r1.member.id = :memberId and (r1.status = 'WAITING' or r1.status = 'CREATED')
        order by r1.date
        """)
    List<ReservationWithRank> findMyReservations(Long memberId);

    Optional<Reservation> findByIdAndStatus(Long id, Status status);
}
