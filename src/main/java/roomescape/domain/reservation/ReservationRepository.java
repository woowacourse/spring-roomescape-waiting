package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findReservationById(long id);

    Optional<Reservation> findReservationBySlotId(Long slotId);

    List<Reservation> findAllReservations();

    List<Reservation> findAllByName(String name);

    boolean isExistBySlot(long slotId);

    Long insert(Reservation reservation);

    void updateName(Long id, String name);

    long update(Long id, String name, Long slotId, LocalDateTime createdAt);

    int updatePaid(Long id, boolean paid);

    List<Reservation> findUnpaidCreatedBefore(LocalDateTime threshold);

    int deleteUnpaidByIds(List<Long> ids);

    long delete(Long id);
}
