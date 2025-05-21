package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.infrastructure.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final List<Waiting> waitings = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public Waiting save(Waiting waiting) {
        Waiting newWaiting = Waiting.builder()
                .id(index.getAndIncrement())
                .date(waiting.getDate())
                .timeSlot(waiting.getTimeSlot())
                .member(waiting.getMember())
                .theme(waiting.getTheme()).build();
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme,
                                                            TimeSlot timeSlot) {
        return waitings.stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getMember().equals(member))
                .filter(waiting -> waiting.getTheme().equals(theme))
                .anyMatch(waiting -> waiting.getTimeSlot().equals(timeSlot));
    }
}
