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

    int updatePaid(Long id, boolean paid);

    long update(Long id, String name, Long slotId, LocalDateTime createdAt);

    long delete(Long id);

    void deleteUnpaidByIds(List<Long> ids);

    List<Reservation> findUnpaidCreatedBefore(LocalDateTime dateTime);
}
