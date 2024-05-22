package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exceptions.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {

    boolean existsByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime reservationTime, Theme theme);

    Optional<Reservation> findByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime reservationTime,
                                                               Theme theme);

    List<Reservation> findByThemeAndMember(Theme theme, Member member);

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다. id = " + id));
    }
}
