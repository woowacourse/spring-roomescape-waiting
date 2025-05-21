package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
                    select new roomescape.waiting.controller.response.WaitingInfoResponse(
                        w.id,
                        w.reserver.name,
                        w.theme.name,
                        w.reservationDatetime.reservationDate.date,
                        w.reservationDatetime.reservationTime.startAt
                    )
                    from Waiting w
            """)
    List<WaitingInfoResponse> getAll();

    @Query("""
            select new roomescape.waiting.dto.WaitingWithRank(
                w,
                count(w2)
            )
            from Waiting w
            join ReservationTime rt on rt = w.reservationDatetime.reservationTime
            join Waiting w2 on
                w2.reservationDatetime.reservationTime = rt
                and w2.reservationDatetime.reservationDate.date = w.reservationDatetime.reservationDate.date
                and w2.id <= w.id
            where w.reserver.id = :memberId
            group by w
            """)
    List<WaitingWithRank> findWithRankByMemberId(@Param("memberId") Long memberId);


    @Query("""
            select exists
            (select w from Waiting w
                where w.reserver.id = :memberId
                and w.reservationDatetime.reservationDate.date = :date
                and w.reservationDatetime.reservationTime.id = :timeId
            )
            """)
    boolean existsByMemberIdAndDateAndTimeId(@Param("memberId") Long memberId,
                                             @Param("date") LocalDate date,
                                             @Param("timeId") Long timeId);


    @Query("""
            select exists(
            select w from Waiting w
            where w.reservationDatetime.reservationDate.date = :date
            and w.reservationDatetime.reservationTime.id = :timeId
            )
            """)
    boolean existsByDateAndTimeId(@Param("date") LocalDate date, @Param("timeId") Long timeId);

    @Query("""
            select  exists (
                select w
                from Waiting w
                where w.reserver.id = :memberId
                and w.id = :id
            )
            """)
    boolean existsByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

    @Query("""
                select w
                from Waiting w
                join fetch w.reserver
                where w.reservationDatetime.reservationDate.date = :date
                  and w.reservationDatetime.reservationTime.id = :timeId
                order by w.id asc
            """)
    List<Waiting> findByDateAndTimeId(
            @Param("date") LocalDate date,
            @Param("timeId") Long timeId
    );

}
