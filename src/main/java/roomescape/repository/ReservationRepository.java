package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.repository.dto.WaitingReservationResponse;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByMemberAndThemeAndDateAndTime(Member member,
                                                 Theme theme,
                                                 LocalDate date,
                                                 ReservationTime reservationTime);

    boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, ReservationTime reservationTime);

    Optional<Reservation> findByIdAndMemberAndStatus(long id, Member member, ReservationStatus reservationStatus);

    Optional<Reservation> findByIdAndStatus(long id, ReservationStatus status);

    Optional<Reservation> findFirstByThemeAndDateAndTimeAndStatus(Theme theme,
                                                                  LocalDate date,
                                                                  ReservationTime reservationTime,
                                                                  ReservationStatus reservationStatus);

    @Query("""
            SELECT new roomescape.repository.dto.ReservationRankResponse
            (r.id, r.theme.name, r.date, r.time.startAt, r.status, (SELECT count(r2) AS waiting_rank
            FROM Reservation r2
            WHERE r.createdAt >= r2.createdAt AND r.time = r2.time AND r.date = r2.date AND r.theme = r2.theme)
            )
            FROM Reservation r
            WHERE r.member = :member
            """)
    List<ReservationRankResponse> findReservationRankByMember(Member member);

    @Query("""
            SELECT new roomescape.repository.dto.WaitingReservationResponse
            (r.id, r.member.name, r.theme.name, r.date, r.time)
            FROM Reservation r
            WHERE r.status = :reservationStatus
            """)
    List<WaitingReservationResponse> findReservationByStatus(ReservationStatus reservationStatus);
}
