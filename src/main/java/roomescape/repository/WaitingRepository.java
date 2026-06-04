package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface WaitingRepository {

    Reservation save(Reservation reservation);

    boolean hasWaitingOnSlot(String username, LocalDate date, Long timeId, Long themeId);

    long countWaitingsBefore(Reservation targetWaiting);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findFirstWaiting(LocalDate date, Long timeId, Long themeId);

    void deleteById(Long id);
}
