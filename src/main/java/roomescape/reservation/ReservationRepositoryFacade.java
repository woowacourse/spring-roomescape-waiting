package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public interface ReservationRepositoryFacade {
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();
    List<Reservation> findAllByMember(Member member);
    List<Reservation> findAllByThemeAndDate(Theme theme, LocalDate date);
    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);
    List<Reservation> findAllByReservationStatus(ReservationStatus reservationStatus);
    List<Reservation> findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByAsc(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            ReservationStatus reservationStatus
    );

    void deleteById(Long id);

    boolean existsByReservationTime(ReservationTime reservationTime);
    boolean existsByDateAndReservationTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    );
    boolean existsByReservationTimeAndDateAndThemeAndReservationStatus(
            ReservationTime reservationTime,
            LocalDate date,
            Theme theme,
            ReservationStatus reservationStatus
    );
}
