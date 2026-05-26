package roomescape.support.fake;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.userreservation.UserReservation;
import roomescape.domain.userreservation.UserReservationRepository;
import roomescape.domain.userreservation.WaitingStatus;

public class FakeUserReservationRepository implements UserReservationRepository {

    private final Map<Long, UserReservation> storage = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public UserReservation save(UserReservation userReservation) {
        Long id = userReservation.getId();
        if (id == null) {
            id = sequence++;
        } else {
            sequence = Math.max(sequence, id + 1);
        }
        UserReservation savedUserReservation = UserReservation.createWithId(id, userReservation);
        storage.put(id, savedUserReservation);
        return savedUserReservation;
    }

    @Override
    public List<UserReservation> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<UserReservation> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<UserReservation> findByUserId(Long userId) {
        return storage.values().stream()
            .filter(userReservation -> userId.equals(userReservation.getUserId()))
            .toList();
    }

    @Override
    public Long countByReservationId(Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> reservationId.equals(userReservation.getReservationId()))
            .count();
    }

    @Override
    public boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> userId.equals(userReservation.getUserId()))
            .filter(userReservation -> reservationId.equals(userReservation.getReservationId()))
            .anyMatch(userReservation -> userReservation.getStatus() != WaitingStatus.CANCELED);
    }
}
