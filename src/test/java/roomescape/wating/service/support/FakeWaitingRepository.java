package roomescape.wating.service.support;

import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.NoReservationForWaitingException;
import roomescape.wating.domain.exception.WaitingSlotDuplicateException;
import roomescape.wating.repository.WaitingRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
                Date.valueOf(waiting.getReservationDate()),
                waiting.getCreatedAt(),
                waiting.getTime(),
                waiting.getTheme()
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
    public Optional<Waiting> findEarliestBySlot(final LocalDate date, final long timeId, final long themeId) {
        return waitings.stream()
                .filter(w -> w.getReservationDate().equals(date))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getTheme().getId().equals(themeId))
                .min(Comparator.comparing(Waiting::getCreatedAt));
    }

    @Override
    public int countEarlierWaitingsInSlot(
            final LocalDate date,
            final long timeId,
            final long themeId,
            final LocalDateTime createdAt
    ) {
        return (int) waitings.stream()
                .filter(w -> w.getReservationDate().equals(date))
                .filter(w -> w.getTime().getId().equals(timeId))
                .filter(w -> w.getTheme().getId().equals(themeId))
                .filter(w -> w.getCreatedAt().isBefore(createdAt))
                .count();
    }

    @Override
    public List<Waiting> findAllByCustomerNameAndReservationDateTimeAfter(
            final String customerName,
            final LocalDateTime now
    ) {
        return waitings.stream()
                .filter(waiting -> waiting.isOwnedBy(customerName))
                .filter(waiting -> LocalDateTime.of(
                        waiting.getReservationDate(),
                        waiting.getTime().getStartAt()
                ).isAfter(now))
                .toList();
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
