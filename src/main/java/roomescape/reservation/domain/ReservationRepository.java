package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void deleteById(Long id);

    List<Reservation> findAllBy(LocalDate date, Long themeId);

    List<Reservation> findAllBy(Long memberId, Long themeId, LocalDate from, LocalDate to);

    List<Reservation> findAllBy(Long memberId);

    List<Reservation> findAll();

    boolean existsByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);

    Optional<Reservation> findBy(LocalDate date, Long timeId, Long themeId);
}
