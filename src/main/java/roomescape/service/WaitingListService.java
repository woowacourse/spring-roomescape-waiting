package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListDeleteCommand;
import roomescape.dto.WaitingListResult;
import roomescape.exception.BusinessException;
import roomescape.exception.DatabaseException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

import java.util.List;

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
                waitingList.getReservationDate().getDate(), findReservationTime.getId(), findTheme.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }

        if (waitingListRepository.existsByNameAndDateAndTimeAndTheme(
                waitingList.getName(), waitingList.getReservationDate().getDate(), findTheme.getId(), findReservationTime.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        try {
            final WaitingList savedWaitingList = waitingListRepository.save(waitingList);
            final int waitingOrder = waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(savedWaitingList);
            return WaitingListResult.from(savedWaitingList, waitingOrder);
        } catch (final DataAccessException e) {
            throw new DatabaseException(ErrorCode.UNIQUE_CONSTRAINT_VIOLATION);
        }
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
        if (waitingList.getReservationDate().isToday() && waitingList.getReservationTime().isBefore()) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }
}
