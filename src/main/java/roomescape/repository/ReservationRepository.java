package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    List<Reservation> findByName(String name);

    List<Reservation> findBySlotId(long slotId);

    List<Reservation> findBySlotIds(List<Long> slotIds);

    Reservation save(Reservation reservation);

    void deleteById(long id);

    Optional<Reservation> findReservedBySlot(LocalDate date, long timeId, long themeId);

    void update(Reservation reservation);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);
}
