package roomescape.support.fake;

import java.util.ArrayList;
import java.util.Comparator;
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
            .filter(userReservation -> userId.equals(userReservation.getUser().getId()))
            .toList();
    }

    @Override
    public Long countByReservationId(Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> reservationId.equals(userReservation.getReservation().getId()))
            .count();
    }

    @Override
    public List<UserReservation> findAllByReservationIdOrder(Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> reservationId.equals(userReservation.getReservation().getId()))
            .filter(userReservation -> userReservation.getStatus() == WaitingStatus.WAITING)
            .sorted(Comparator.comparing(UserReservation::getUpdatedAt)
                .thenComparing(UserReservation::getId))
            .toList();
    }

    @Override
    public Optional<UserReservation> update(Long id, UserReservation userReservation) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        UserReservation updatedUserReservation = UserReservation.createWithId(id, userReservation);
        storage.put(id, updatedUserReservation);
        return Optional.of(updatedUserReservation);
    }

    @Override
    public void updateStatus(Long id, WaitingStatus status) {
        UserReservation userReservation = storage.get(id);
        if (userReservation == null) {
            return;
        }
        UserReservation updatedUserReservation = UserReservation.of(
            userReservation.getId(),
            userReservation.getReservation(),
            userReservation.getUser(),
            userReservation.getWaitingNumber(),
            status,
            userReservation.getCreatedAt(),
            userReservation.getUpdatedAt()
        );
        storage.put(id, updatedUserReservation);
    }

    @Override
    public boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId) {
        return storage.values().stream()
            .filter(userReservation -> userId.equals(userReservation.getUser().getId()))
            .filter(userReservation -> reservationId.equals(userReservation.getReservation().getId()))
            .anyMatch(userReservation -> userReservation.getStatus() != WaitingStatus.CANCELED);
    }

    @Override
    public void updateWaitingNumbers(List<UserReservation> userReservations) {
        for (int index = 0; index < userReservations.size(); index++) {
            UserReservation userReservation = userReservations.get(index);
            WaitingStatus status = index == 0 ? WaitingStatus.CONFIRMED : WaitingStatus.WAITING;
            UserReservation rerankedUserReservation = UserReservation.of(
                userReservation.getId(),
                userReservation.getReservation(),
                userReservation.getUser(),
                (long) index,
                status,
                userReservation.getCreatedAt(),
                userReservation.getUpdatedAt()
            );
            storage.put(userReservation.getId(), rerankedUserReservation);
        }
    }
}
