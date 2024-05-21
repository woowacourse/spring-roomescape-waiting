package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDetail;
import roomescape.domain.Status;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

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
            @Param("start") LocalDate start, @Param("end") LocalDate end,
            @Param("memberId") Long memberId, @Param("themeId") Long themeId
    );

    boolean existsByDetailAndMember(ReservationDetail detail, Member member);

    boolean existsByDetailAndStatus(ReservationDetail reservationDetail, Status status);
}
