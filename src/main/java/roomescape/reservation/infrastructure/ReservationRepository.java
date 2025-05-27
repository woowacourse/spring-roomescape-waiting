package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long>, ReservationRepositoryCustom {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    boolean existsByThemeId(Long themeId);

    Optional<Reservation> findByReservationTimeAndTheme(ReservationTime time, Theme theme);

    List<Reservation> findByReservationTimeDateAndTheme(LocalDate date, Theme theme);

    @Query("""
                SELECT r
                FROM Reservation r
                JOIN FETCH r.member
                JOIN FETCH r.theme
                JOIN FETCH r.reservationTime
                WHERE r.member.id = :memberId
            """)
    List<Reservation> findByMemberId(Long memberId);

    boolean existsByReservationTimeAndMemberAndTheme(ReservationTime time, Member member, Theme theme);

    boolean existsByReservationTimeAndTheme(ReservationTime time, Theme theme);

    void delete(Reservation reservation);

    boolean existsByReservationTimeTimeSlotId(Long timeSlotId);
}

