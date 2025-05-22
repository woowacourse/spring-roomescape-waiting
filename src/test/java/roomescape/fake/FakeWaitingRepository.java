package roomescape.fake;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepositoryInterface;
import roomescape.theme.domain.Theme;

public class FakeWaitingRepository implements WaitingRepositoryInterface {

    private final Map<Long, Waiting> waitings = new HashMap<>();
    private long sequence = 0;

    @Override
    public Waiting save(final Waiting waiting) {
        sequence++;
        final Waiting newWaiting = new Waiting(
                sequence,
                waiting.getMember(),
                waiting.getTime(),
                waiting.getTheme(),
                waiting.getDate()
        );
        waitings.put(sequence, newWaiting);

        return newWaiting;
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme) {

        return waitings.values().stream()
                .anyMatch(waiting ->
                        waiting.getDate().equals(date) &&
                                waiting.getTime().equals(reservationTime) &&
                                waiting.getTheme().equals(theme)
                );
    }

    @Override
    public List<Waiting> findByMember(final Member member) {
        return waitings.values().stream()
                .filter(waiting -> waiting.getMember().equals(member))
                .toList();
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return Optional.ofNullable(waitings.get(id));
    }

    @Override
    public void deleteById(final Long id) {
        waitings.remove(id);
    }

    @Override
    public long countBefore(final Theme theme, final LocalDate date, final ReservationTime time, final Long id) {
        return waitings.values().stream()
                .filter(waiting -> waiting.getTheme().equals(theme) &&
                        waiting.getDate().isEqual(date) &&
                        waiting.getTime().equals(time) &&
                        waiting.getId() < id)
                .count();
    }
}
