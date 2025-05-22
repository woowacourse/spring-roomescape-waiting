package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByMemberId(Long memberId);

    void deleteById(Long id);

    List<Reservation> findBy(LocalDate date, Long themeId);

    List<Reservation> findBy(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findBy(Long themeId, LocalDate date);

    List<Reservation> findAll();

    boolean existsById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
