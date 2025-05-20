package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends Repository<Reservation, Long>, ReservationRepositoryCustom {
    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByThemeId(Long themeId);

    Optional<Reservation> findByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByReservationTimeId(Long reservationTimeId);

    List<Reservation> findByMemberId(Long memberId);
}

