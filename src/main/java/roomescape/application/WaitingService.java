package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.NotFoundException;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final EntityLookupService entityLookupService;

    public WaitingService(
            final ReservationRepository reservationRepository,
            final WaitingRepository waitingRepository,
            final EntityLookupService entityLookupService
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.entityLookupService = entityLookupService;
    }

    public Waiting saveWaiting(final User user,
                               final LocalDate date,
                               final long timeId,
                               final long themeId) {
        TimeSlot timeSlot = entityLookupService.getTimeSlotById(timeId);
        Theme theme = entityLookupService.getThemeById(themeId);

        validateDuplicateWaiting(date, timeSlot.id(), theme.id(), user.id());
        validateNotAlreadyReserved(date, timeSlot.id(), theme.id(), user.id());

        Waiting waiting = Waiting.register(user, date, timeSlot, theme);
        return waitingRepository.save(waiting);
    }

    private void validateDuplicateWaiting(final LocalDate date,
                                          final long timeSlotId,
                                          final long themeId,
                                          final long userId) {
        boolean isWaitingExisted =
                waitingRepository.existsByDateAndTimeSlotIdAndThemeIdAndUserId(date, timeSlotId, themeId, userId);

        if (isWaitingExisted) {
            throw new AlreadyExistedException("이미 예약 대기한 내역이 있습니다.");
        }
    }

    private void validateNotAlreadyReserved(final LocalDate date,
                                            final long timeSlotId,
                                            final long themeId,
                                            final long userId) {
        boolean isAlreadyReserved =
                reservationRepository.existsByDateAndTimeSlotIdAndThemeIdAndUserId(date, timeSlotId, themeId, userId);

        if (isAlreadyReserved) {
            throw new BusinessRuleViolationException("해당 테마의 시간대에 이미 예약되어 있습니다.");
        }
    }

    public List<Waiting> findAllWaitings() {
        return waitingRepository.findAll();
    }

    public void removeById(final long id) {
        validateWaitingExists(id);
        waitingRepository.deleteById(id);
    }

    private void validateWaitingExists(long id) {
        boolean isWaitingExisted = waitingRepository.existsById(id);

        if (!isWaitingExisted) {
            throw new NotFoundException("존재하지 않는 예약 대기입니다.");
        }
    }
}
