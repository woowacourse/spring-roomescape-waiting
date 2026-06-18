package roomescape.waiting.service.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.repository.dto.WaitingWithRank;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();

    @Override
    public Waiting save(final Waiting waiting) {
        final Waiting savedWaitingWithId = Waiting.of(
            1L,
            waiting.getCustomerName().name(),
            waiting.getReservationDate(),
            waiting.getCreatedAt(),
            waiting.getTime(),
            waiting.getTheme()
        );
        waitings.add(savedWaitingWithId);
        return savedWaitingWithId;
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
    public Optional<Waiting> findEarliestBySlotForUpdate(final LocalDate date, final long timeId, final long themeId) {
        return waitings.stream()
            .filter(w -> w.getReservationDate().equals(date))
            .filter(w -> w.getTime().getId().equals(timeId))
            .filter(w -> w.getTheme().getId().equals(themeId))
            .min(Comparator.comparing(Waiting::getCreatedAt));
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByCustomerNameAndReservationDateTimeAfter(
        final String customerName,
        final LocalDateTime now
    ) {
        List<Waiting> customerWaitings = waitings.stream()
            .filter(w -> w.isOwnedBy(customerName))
            .filter(w -> LocalDateTime.of(
                w.getReservationDate(),
                w.getTime().getStartAt()
            ).isAfter(now))
            .toList();

        return customerWaitings.stream()
            .map(w -> {
                int rank = (int) waitings.stream()
                    .filter(o -> o.getReservationDate().equals(w.getReservationDate()))
                    .filter(o -> o.getTime().getId().equals(w.getTimeId()))
                    .filter(o -> o.getTheme().getId().equals(w.getThemeId()))
                    .filter(o -> o.getCreatedAt().isBefore(w.getCreatedAt()))
                    .count() + 1;
                return new WaitingWithRank(w, rank);
            })
            .toList();
    }

    @Override
    public List<WaitingWithRank> findAllWithRank() {
        return waitings.stream()
            .map(w -> {
                int rank = (int) waitings.stream()
                    .filter(o -> o.getReservationDate().equals(w.getReservationDate()))
                    .filter(o -> o.getTime().getId().equals(w.getTimeId()))
                    .filter(o -> o.getTheme().getId().equals(w.getThemeId()))
                    .filter(o -> o.getCreatedAt().isBefore(w.getCreatedAt()))
                    .count() + 1;
                return new WaitingWithRank(w, rank);
            })
            .toList();
    }

    @Override
    public boolean existsBySlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        return waitings.stream()
            .anyMatch(waiting ->
                waiting.getReservationDate().isEqual(reservationDate)
                    && waiting.getTimeId() == timeId
                    && waiting.getThemeId() == themeId);
    }

    public void add(final Waiting waiting) {
        waitings.add(waiting);
    }
}
