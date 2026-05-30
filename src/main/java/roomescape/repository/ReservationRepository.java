package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    boolean existsBySlot(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<Long> findReservedTimeIdsByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByName(String name);

    List<Reservation> findAll(int offset, int limit);

    long count();

    void deleteById(Long id);
}
