package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.result.ReservationWaitingOrderResult;

public interface JpaReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    Optional<ReservationWaiting> findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
            LocalDate date,
            Long timeId,
            Long themeId
    );

    List<ReservationWaiting> findByName(String name);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByTime_Id(Long timeId);

    boolean existsByNameAndDateAndTime_IdAndTheme_Id(
            String name,
            LocalDate date,
            Long timeId,
            Long themeId
    );

    @Query("""
                    select new roomescape.repository.result.ReservationWaitingOrderResult(
                        rw.id,
                        rw.date,
                        rw.time.id,
                        rw.theme.id,
                        rw.createdAt
                    )
                    from ReservationWaiting rw
                    where rw in :waitings
                    order by
                        rw.date,
                        rw.time.id,
                        rw.theme.id,
                        rw.createdAt,
                        rw.id
            """)
    List<ReservationWaitingOrderResult> findOrderResultsBy(
            @Param("waitings") List<ReservationWaiting> waitings
    );

    @Query("""
                select count(earlier)
                from ReservationWaiting earlier, ReservationWaiting target
                where target.id = :id
                  and earlier.date = target.date
                  and earlier.time = target.time
                  and earlier.theme = target.theme
                  and (
                      earlier.createdAt < target.createdAt
                      or (
                          earlier.createdAt = target.createdAt
                          and earlier.id < target.id
                      )
                  )
            """)
    long countEarlierWaitings(@Param("id") Long id);

}
