package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListCreateCommand;
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

        WaitingList waitingList = WaitingList.create(createCommand.date(), createCommand.name(), findTheme, findReservationTime);
        if (waitingList.getReservationDate().isPast()) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
        if (waitingList.getReservationDate().isToday() && findReservationTime.isBefore()) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }

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
}
