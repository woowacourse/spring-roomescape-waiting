package roomescape.reservation.repository;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.dto.WaitingWithRankDto;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId,
                                  final Long memberId) {
        return waitings.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTime().getId(), timeId)
                        && Objects.equals(reservation.getTheme().getId(), themeId)
                        && Objects.equals(reservation.getMember().getId(), memberId));
    }

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId) {
        return waitings.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTime().getId(), timeId)
                        && Objects.equals(reservation.getTheme().getId(), themeId));
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getId(), id))
                .findFirst();
    }

    @Override
    public List<Waiting> findAll() {
        return new ArrayList<>(waitings);
    }

    @Override
    public List<WaitingWithRankDto> findWithRankByMemberId(final Long memberId) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getMember().getId(), memberId))
                .map(waiting -> new WaitingWithRankDto(
                        waiting.getId(),
                        waiting.getTheme().getName().getValue(),
                        Date.valueOf(waiting.getDate().getValue()),
                        Time.valueOf(waiting.getTime().getStartAt()),
                        (long) (waitings.indexOf(waiting) + 1) // Assuming rank is based on the order in the list
                ))
                .toList();
    }

    @Override
    public Waiting save(final Waiting waiting) {
        Waiting saved = Waiting.withId(
                index.getAndIncrement(),
                waiting.getMember(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme());

        waitings.add(saved);

        return saved;
    }

    @Override
    public Optional<Waiting> findEarliestByParams(final ReservationDate date, final Long timeId, final Long themeId) {
        return waitings.stream()
                .filter(waiting -> Objects.equals(waiting.getDate(), date)
                        && Objects.equals(waiting.getTime().getId(), timeId)
                        && Objects.equals(waiting.getTheme().getId(), themeId))
                .min(Comparator.comparing(Waiting::getCreatedAt));
    }

    @Override
    public void deleteById(final Long id) {
        Waiting waiting = waitings.stream()
                .filter(value -> Objects.equals(value.getId(), id))
                .findFirst()
                .orElseThrow(NotFoundException::new);

        waitings.remove(waiting);
    }
}
