package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.entity.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    List<ReservationTime> findAll();

    // TODO: 테스트 코드 짜기, 응답 DTO로 변환 가능한지 확인
//    @Query(value = """
//            SELECT rt.id, rt.startAt
//            FROM ReservationTime rt
//            WHERE rt.id NOT IN (
//                SELECT rt.id
//                FROM Reservation r
//                WHERE r.date = :date AND r.theme.id = :themeId
//            )
//            """)
//    List<ReservationTime> findAvailableTimes(@Param("date") LocalDate date, @Param("themeId") Long themeId);

    @Query(value = """
            SELECT
                rt.id,
                rt.start_at
            FROM reservation_time rt
            JOIN (
                SELECT id, time_id
                FROM reservation
                WHERE date = :date AND theme_id = :themeId
            ) r ON r.time_id = rt.id
            ORDER BY rt.start_at
            """, nativeQuery = true)
    List<ReservationTime> findAllReservedTimeByDateAndThemeId(@Param("date") LocalDate date,
                                                              @Param("themeId") Long themeId);
}
