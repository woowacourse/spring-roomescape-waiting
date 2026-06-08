package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation update(Long id, ReservationSlot slot);

    void delete(Long id);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByName(String name);

    Optional<Reservation> findBySlotWithLock(ReservationSlot slot);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);

    boolean existsBySlot(ReservationSlot slot);
}
