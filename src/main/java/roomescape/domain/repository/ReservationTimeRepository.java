package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository extends Repository<ReservationTime, LocalTime> {
    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime time);

    void delete(ReservationTime time);

    void deleteAll();

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
