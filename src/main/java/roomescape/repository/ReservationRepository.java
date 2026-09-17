package roomescape.repository;

import roomescape.domain.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(long id);

    Optional<Reservation> findByIdForUpdate(long id);

    Reservation save(Reservation reservation);

    void flush();

    void deleteById(long id);

    boolean existsConfirmedByThemeSlotId(long themeSlotId);

    List<Reservation> findByName(String name);

    boolean updateStatus(Reservation reservation, String expectedStatus);

    void updateThemeSlot(Reservation reservation);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeSlotIdAndMemberName(String name, Long themeSlotId);
}
