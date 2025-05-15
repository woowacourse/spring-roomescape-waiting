package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    @Query("SELECT r                           "
            + "FROM Reservation r                "
            + "JOIN r.time t "
            + "JOIN r.theme th                   "
            + "JOIN r.member m                   "
            + "WHERE th.id        = :themeId     "
            + "  AND m.id         = :memberId    "
            + "  AND r.date BETWEEN :startDate AND :endDate ")
    List<Reservation> findFilteredReservations(@Param("themeId") final Long themeId,
                                               @Param("memberId") final Long memberId,
                                               @Param("startDate") final LocalDate startDate,
                                               @Param("endDate") final LocalDate endDate);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long themeId, final Long timeId);

    @Query("SELECT new roomescape.reservationtime.dto.response.AvailableReservationTimeResponse(rt.id, rt.startAt, "
            + "CASE WHEN r.id IS NOT NULL THEN true ELSE false END AS already_booked) "
            + "FROM ReservationTime AS rt "
            + "LEFT JOIN Reservation r ON rt.id = r.time.id AND r.date = :date AND r.theme.id = :themeId "
            + "ORDER BY rt.startAt")
    List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(@Param("date") LocalDate date,
                                                                           @Param("themeId") Long themeId);

    List<Reservation> findByMemberId(Long id);
}
