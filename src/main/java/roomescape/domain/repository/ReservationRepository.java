package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;

public interface ReservationRepository extends Repository<Reservation, Long> {
    List<Reservation> findAll();

    @Query(""" 
            select r from Reservation r
            join fetch r.member m
            join fetch r.detail d
            where r.detail.date >= :start
            and r.detail.date <= :end
            and m.id = :memberId
            and d.theme.id = :themeId
            """)
    List<Reservation> findByPeriodAndThemeAndMember(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("memberId") Long memberId,
            @Param("themeId") Long themeId);
}
