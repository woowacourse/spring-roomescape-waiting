package roomescape.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationItem;
import roomescape.domain.ReservationItemRepository;
import roomescape.repository.jpa.ReservationItemJpaRepository;

@RequiredArgsConstructor
@Repository
public class ReservationItemRepositoryImpl implements ReservationItemRepository {

    private final ReservationItemJpaRepository reservationItemJpaRepository;

    @Override
    public ReservationItem save(ReservationItem reservationItem) {
        return reservationItemJpaRepository.save(reservationItem);
    }
}
