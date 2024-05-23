package roomescape.infra.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservationdetail.ReservationTime;
import roomescape.domain.reservationdetail.ReservationTimeRepository;

public interface ReservationTimeJpaRepository extends
        ReservationTimeRepository,
        Repository<ReservationTime, LocalTime> {

    @Override
    ReservationTime save(ReservationTime time);

    @Override
    Optional<ReservationTime> findById(Long id);

    @Override
    List<ReservationTime> findAll();

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

    @Override
    boolean existsByStartAt(LocalTime startAt);

    @Override
    void delete(ReservationTime time);
}
