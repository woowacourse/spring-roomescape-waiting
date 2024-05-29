package roomescape.infra.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.ReservationTimeRepository;
import roomescape.exception.time.NotFoundReservationTimeException;

public interface ReservationTimeJpaRepository extends
        ReservationTimeRepository,
        Repository<ReservationTime, LocalTime> {

    @Override
    default ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    @Override
    @Query("""
            select d.time from ReservationDetail d
            where d.date = :date
            and d.theme.id = :themeId
            and exists (
                select 1 from Reservation r
                where r.detail.id = d.id
                and r.status = 'RESERVED'
            )
            """)
    List<ReservationTime> findAllReservedTimeByDateAndThemeId(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );
}
