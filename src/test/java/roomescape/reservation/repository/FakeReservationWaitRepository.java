package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

public class FakeReservationWaitRepository implements ReservationWaitRepository {
    private final List<ReservationWait> reservationWaits = new CopyOnWriteArrayList<>();

    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public List<ReservationWait> findAll() {
        return new CopyOnWriteArrayList<>(reservationWaits);
    }

    @Override
    public List<ReservationWaitWithRankResponse> findWithRankByInfoMemberId(final Long memberId) {
        final List<ReservationWait> filtered = reservationWaits.stream()
                .filter(wait -> wait.getMember().getId().equals(memberId))
                .toList();

        return filtered.stream()
                .map(wait -> new ReservationWaitWithRankResponse(
                        wait,
                        (long) (filtered.indexOf(wait) + 1)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReservationWait> findByParamsAt(
            final ReservationDate date,
            final Long timeId,
            final Long themeId,
            final int index
    ) {
        final List<ReservationWait> filtered = reservationWaits.stream()
                .filter(wait -> wait.getDate().equals(date) &&
                                wait.getTime().getId().equals(timeId) &&
                                wait.getTheme().getId().equals(themeId))
                .toList();

        if (index < 0 || index >= filtered.size()) {
            return Optional.empty();
        }
        return Optional.of(filtered.get(index));
    }

    @Override
    public ReservationWait save(final ReservationWait reservationWait) {
        final ReservationWait saved = ReservationWait.withId(
                index.getAndIncrement(),
                reservationWait.getMember(),
                reservationWait.getDate(),
                reservationWait.getTime(),
                reservationWait.getTheme()
        );

        reservationWaits.add(saved);
        return saved;
    }

    @Override
    public void deleteById(final Long id) {
        reservationWaits.removeIf(reservationWait -> reservationWait.getId().equals(id));
    }

    @Override
    public boolean existsByInfoDateAndInfoTimeIdAndInfoThemeIdAndInfoMemberId(
            final ReservationDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId
    ) {
        return reservationWaits.stream().anyMatch(wait ->
                wait.getDate().equals(date) &&
                wait.getTime().getId().equals(timeId) &&
                wait.getTheme().getId().equals(themeId) &&
                wait.getMember().getId().equals(memberId)
        );
    }
}
