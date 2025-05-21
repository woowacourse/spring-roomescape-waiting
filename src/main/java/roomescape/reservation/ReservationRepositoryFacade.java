package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

public interface ReservationRepositoryFacade {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();
    List<Reservation> findAllByMember(Member member);
    List<Reservation> findAllByThemeAndDate(Theme theme, LocalDate date);
    List<Reservation> findAllByMemberAndThemeAndDateBetween(Member member, Theme theme, LocalDate from, LocalDate to);

    void deleteById(Long id);

    boolean existsById(Long id);
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
