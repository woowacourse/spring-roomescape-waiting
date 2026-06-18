package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.WaitingRank;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    int delete(Long id);

    boolean existsOccupiedBySlotId(Long slotId);

    Optional<Reservation> findFirstWaitingBySlotId(Long slotId);

    Reservation updateStatus(Reservation reservation);

    List<Reservation> findReservedByName(String name);

    List<WaitingRank> findWaitingRanksByName(String name);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);
}
