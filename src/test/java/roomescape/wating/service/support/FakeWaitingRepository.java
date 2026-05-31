package roomescape.wating.service.support;

import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.NoReservationForWaitingException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.repository.dto.WaitingWithRank;

import java.time.LocalDateTime;
import java.util.*;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private Waiting savedWaiting;
    private RuntimeException saveException;

    @Override
    public Long save(final Waiting waiting) {
        if (saveException != null) {
            throw saveException;
        }
        savedWaiting = waiting;
        final Waiting savedWaitingWithId = Waiting.of(
                1L,
                waiting.getCustomerName().name(),
                waiting.getSlot(),
                waiting.getCreatedAt()
        );
        waitings.add(savedWaitingWithId);
        return savedWaitingWithId.getId();
    }

    @Override
    public boolean deleteById(final long id) {
        return waitings.removeIf(waiting -> waiting.getId().equals(id));
    }

    @Override
    public Optional<Waiting> findById(final long id) {
        return waitings.stream()
                .filter(waiting -> waiting.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findEarliestBySlotId(final Long slotId) {
        return waitings.stream()
                .filter(w -> Objects.equals(w.getSlotId(), slotId))
                .min(Comparator.comparing(Waiting::getCreatedAt)
                        .thenComparing(Waiting::getId));
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByCustomerNameAndReservationDateTimeAfter(
            final String customerName,
            final LocalDateTime now
    ) {
        return waitings.stream()
                .filter(waiting -> waiting.isOwnedBy(customerName))
                .filter(waiting -> LocalDateTime.of(
                        waiting.getReservationDate(),
                        waiting.getTime().getStartAt()
                ).isAfter(now))
                .map(waiting -> new WaitingWithRank(waiting, calculateRank(waiting)))
                .toList();
    }

    private int calculateRank(final Waiting waiting) {
        return (int) waitings.stream()
                .filter(candidate -> Objects.equals(candidate.getSlotId(), waiting.getSlotId()))
                .filter(candidate -> candidate.getCreatedAt().isBefore(waiting.getCreatedAt())
                        || (candidate.getCreatedAt().equals(waiting.getCreatedAt())
                        && candidate.getId() < waiting.getId()))
                .count() + 1;
    }

    public void add(final Waiting waiting) {
        waitings.add(waiting);
    }

    public void failToSaveByDuplicatedWaiting() {
        saveException = new WaitingSlotDuplicateException();
    }

    public void failToSaveByNoReservation() {
        saveException = new NoReservationForWaitingException();
    }
}
