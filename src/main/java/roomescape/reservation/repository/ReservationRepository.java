package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void deleteById(Long id);

    boolean existsByReservationDateAndReservationTimeId(ReservationDate reservationDate, Long timeId);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findByFilter(Long memberId, Long themeId, LocalDate start, LocalDate end);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Theme> findTopThemesByReservationCount(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
