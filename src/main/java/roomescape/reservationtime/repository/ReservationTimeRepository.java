package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import roomescape.exception.NotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다. id: " + id));
    }

    @Query("""
            select distinct
              new roomescape.reservationtime.dto.AvailableReservationTimeResponse(
                rt.id, rt.startAt, (r.id is not null) as alreadyBooked
              )
            from ReservationTime rt
            left join Reservation r
              on rt.id = r.time.id
              and r.date = :date
              and r.theme.id = :themeId
            order by rt.startAt
            """)
    List<AvailableReservationTimeResponse> findAllAvailable(
            @Param("date") final LocalDate date,
            @Param("themeId") final Long themeId
    );
}
