package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll(int offset, int limit);

    long count();

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    void deleteById(Long id);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsBySlot(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByName(String name);
}
