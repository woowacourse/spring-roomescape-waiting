package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public WaitingService(
            final WaitingRepository waitingRepository,
            final TimeSlotRepository timeSlotRepository,
            final ThemeRepository themeRepository,
            final UserRepository userRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    public Waiting saveWaiting(final User user,
                               final LocalDate date,
                               final long timeId,
                               final long themeId) {
        TimeSlot timeSlot = getTimeSlotById(timeId);
        Theme theme = getThemeById(themeId);
        validateDuplicateWaiting(date, timeSlot, theme, user);

        Waiting waiting = Waiting.reserveNewly(user, date, timeSlot, theme);
        return waitingRepository.save(waiting);
    }

    private TimeSlot getTimeSlotById(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타임 슬롯입니다."));
    }

    private Theme getThemeById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    private void validateDuplicateWaiting(final LocalDate date,
                                          final TimeSlot timeSlot,
                                          final Theme theme,
                                          final User user) {
        Optional<Waiting> waiting = waitingRepository.findByDateAndTimeSlotIdAndThemeIdAndUserId
                (date, timeSlot.id(), theme.id(), user.id());

        if (waiting.isPresent()) {
            throw new AlreadyExistedException("이미 예약 대기한 내역이 있습니다.");
        }
    }

    public List<WaitingWithRank> findWaitingByUserId(final long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + userId));

        return waitingRepository.findWaitingWithRankByUserId(user.id());
    }

    public void removeById(final long id) {
        waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));

        waitingRepository.deleteById(id);
    }
}
