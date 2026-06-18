package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndName(long id, String name);

    Optional<Reservation> findByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    // 예약이 생성 시 중복 예약인지 확인
    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId);

    // 예약 수정 시 중복 예약이 되는지 확인 (내 예약 충돌 제거)
    boolean existsByDateAndThemeIdAndTimeIdAndIdNot(LocalDate date, long themeId, long timeId, long reservationId);

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :date AND r.theme.id = :themeId")
    List<Long> findReservedTimeIdsByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") long themeId);
}
