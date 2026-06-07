package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.ReservationCanceledEvent;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListDeleteCommand;
import roomescape.dto.WaitingListResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingListResult create(final WaitingListCreateCommand createCommand) {
        final ReservationTime findReservationTime = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
        final Theme findTheme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));

        final WaitingList waitingList = WaitingList.create(createCommand.name(), createCommand.date(), findReservationTime, findTheme);
        validateFuture(waitingList);

        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                waitingList.getReservationDate().date(), findReservationTime.getId(), findTheme.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }

        if (waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(
                waitingList.getName(), waitingList.getReservationDate().date(), findTheme.getId(), findReservationTime.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        final WaitingList savedWaitingList = waitingListRepository.save(waitingList);
        final int waitingOrder = waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(savedWaitingList);
        return WaitingListResult.from(savedWaitingList, waitingOrder);
    }

    public void delete(final WaitingListDeleteCommand deleteCommand) {
        final WaitingList findWaitingList = waitingListRepository.findById(deleteCommand.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));

        if (!findWaitingList.isOwner(deleteCommand.name())) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }

        validateFuture(findWaitingList);

        final boolean deleted = waitingListRepository.deleteById(deleteCommand.waitingListId());
        if (!deleted) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReservationCanceled(ReservationCanceledEvent event) {
        Optional<WaitingList> nextWaiting = waitingListRepository.findFirstBySlot(
                event.date(), event.timeId(), event.themeId());

        if (nextWaiting.isEmpty()) {
            return;
        }

        WaitingList waiting = nextWaiting.get();
        Reservation newReservation = Reservation.create(
                waiting.getName(),
                waiting.getReservationDate().date(),
                waiting.getReservationTime(),
                waiting.getTheme()
        );

        reservationRepository.save(newReservation);
        waitingListRepository.deleteById(waiting.getId());
    }

    public List<WaitingListResult> getWaitingListByName(final String name) {
        final List<WaitingList> waitingLists = waitingListRepository.findByName(name);
        return waitingLists.stream()
                .map(waitingList -> WaitingListResult.from(
                        waitingList, waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(waitingList)
                        )
                ).toList();
    }

    private static void validateFuture(final WaitingList waitingList) {
        if (waitingList.getReservationDate().isPast()) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
        final LocalTime now = LocalTime.now();
        if (waitingList.getReservationDate().isToday() && waitingList.getReservationTime().isBefore(now)) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }
}
