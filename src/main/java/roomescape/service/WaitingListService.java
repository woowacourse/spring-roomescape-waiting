package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.service.dto.ReservationAvailableEvent;
import roomescape.service.dto.command.WaitingListCreateCommand;
import roomescape.service.dto.command.WaitingListDeleteCommand;
import roomescape.service.dto.result.WaitingListResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WaitingListService {

    private static final Logger failureLog = LoggerFactory.getLogger("waiting.approval.failure");

    private final WaitingListRepository waitingListRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingListResult create(final WaitingListCreateCommand createCommand, final LocalDate today, final LocalTime now) {
        final ReservationTime findReservationTime = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
        final Theme findTheme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));

        final WaitingList waitingList = WaitingList.create(createCommand.name(), createCommand.date(), findReservationTime, findTheme);
        validateFuture(waitingList, today, now);

        final LocalDate date = waitingList.getReservationDate().date();
        final Long timeId = findReservationTime.getId();
        final Long themeId = findTheme.getId();
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)
                && !waitingListRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)
        ) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }

        if (waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(waitingList.getName(), date, timeId, themeId)) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        final WaitingList savedWaitingList = waitingListRepository.save(waitingList);
        final int waitingOrder = waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(savedWaitingList);
        return WaitingListResult.from(savedWaitingList, waitingOrder);
    }

    public void delete(final WaitingListDeleteCommand deleteCommand, final LocalDate today, final LocalTime now) {
        final WaitingList findWaitingList = waitingListRepository.findById(deleteCommand.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));

        if (!findWaitingList.isOwner(deleteCommand.name())) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }

        validateFuture(findWaitingList, today, now);

        final boolean deleted = waitingListRepository.deleteById(deleteCommand.waitingListId());
        if (!deleted) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteWaitingListToReservation(final ReservationAvailableEvent event) {
        try {
            final Optional<WaitingList> nextWaiting = waitingListRepository.findFirstBySlot(
                    event.date(), event.timeId(), event.themeId());

            if (nextWaiting.isEmpty()) {
                return;
            }

            final WaitingList waiting = nextWaiting.get();
            final Reservation newReservation = Reservation.prepare(
                    waiting.getName(),
                    waiting.getReservationDate().date(),
                    waiting.getReservationTime(),
                    waiting.getTheme()
            );

            reservationRepository.save(newReservation);

            final boolean deleted = waitingListRepository.deleteById(waiting.getId());
            if (!deleted) {
                throw new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            failureLog.error(
                    "예약 대기 승인에 실패했습니다. date={}, timeId={}, themeId={}",
                    event.date(), event.timeId(), event.themeId(), e);
            throw e;
        }
    }

    public List<WaitingListResult> getWaitingListByName(final String name) {
        final List<WaitingList> waitingLists = waitingListRepository.findByName(name);
        return waitingLists.stream()
                .map(waitingList -> WaitingListResult.from(
                        waitingList, waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(waitingList)
                        )
                ).toList();
    }

    private static void validateFuture(final WaitingList waitingList, final LocalDate today, final LocalTime now) {
        if (waitingList.getReservationDate().isPast(today)) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }

        if (waitingList.getReservationDate().isSameDay(today) && waitingList.getReservationTime().isBefore(now)) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }
}
