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
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

@RequiredArgsConstructor
@Service
public class WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingListResult create(final WaitingListCreateCommand createCommand) {
        Theme findTheme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
        ReservationTime findReservationTime = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));

        WaitingList waitingList = WaitingList.create(createCommand.name(), createCommand.date(), findTheme, findReservationTime);
        validateFuture(waitingList);

        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                waitingList.getReservationDate().getDate(), findReservationTime.getId(), findTheme.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }

        if (waitingListRepository.existsByNameAndThemeAndDateAndTime(
                waitingList.getName(), findTheme.getId(), waitingList.getReservationDate().getDate(), findReservationTime.getId()
            )
        ) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        try {
            WaitingList savedWaitingList = waitingListRepository.save(waitingList);
            return WaitingListResult.from(savedWaitingList);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }

    public void delete(final WaitingListDeleteCommand deleteCommand) {
        WaitingList findWaitingList = waitingListRepository.findById(deleteCommand.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));
        
        if (!findWaitingList.getName().equals(deleteCommand.name())) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }

        validateFuture(findWaitingList);
        
        boolean deleted = waitingListRepository.deleteById(deleteCommand.waitingListId());
        if (!deleted) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND);
        }
    }

    private static void validateFuture(WaitingList waitingList) {
        if (waitingList.getReservationDate().isPast()) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
        if (waitingList.getReservationDate().isToday() && waitingList.getReservationTime().isBefore()) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }
}
