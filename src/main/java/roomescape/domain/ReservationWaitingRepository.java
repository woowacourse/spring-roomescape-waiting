package roomescape.domain;

import java.util.Optional;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    boolean existsBy(Member member, Slot slot);

    Optional<ReservationWaiting> findById(Long id);

    Optional<ReservationWaiting> findFirstBySlot(Slot slot);

    void deleteById(Long id);
}
