package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

@Repository
public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    List<Reservation> findByName(String name);

    List<Reservation> findWaitingsBySlotId(Long slotId);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    Optional<Reservation> findReservedBySlot(LocalDate date, Long timeId, Long themeId);

    void update(Reservation reservation);

    boolean existsReservedBySlot(LocalDate date, Long timeId, Long themeId);

    boolean existsByNameAndSlotId(String name, Long slotId);

    boolean existsByThemeId(Long themeId);

    boolean existsByTimeId(Long timeId);
}
