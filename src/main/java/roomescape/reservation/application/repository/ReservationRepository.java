package roomescape.reservation.application.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {
    boolean existsByReservationTimeId(Long timeId);

//    boolean existsByDateTime(LocalDateTime reservationDateTime);

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);

    boolean existsByThemeId(Long themeId);

//    List<Long> findBookedTimeIds(LocalDate date, Long themeId);

    List<Long> findTimeIdByDateAndThemeId(LocalDate date, Long themeId);

//    List<Reservation> findReservationsBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

//    List<Reservation>

    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(final Long memberId, final Long themeId,
                                                                final LocalDate fromDate, final LocalDate endDate);
}

/*
1. 흐름 직접 구현(필터 정렬 구현)
2. native sql
 */