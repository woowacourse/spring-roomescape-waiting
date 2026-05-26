package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationWaiting;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationWaitingRepository {
    public List<ReservationWaiting> findByName(String name) {
        return null;
    }

    public Long countEarlierWaitings(ReservationWaiting waiting) {
        return null;
    }

    public Long insert(ReservationWaiting waiting) {
        return null;
    }

    public Optional<ReservationWaiting> findById(Long id) {
        return null;
    }
}
