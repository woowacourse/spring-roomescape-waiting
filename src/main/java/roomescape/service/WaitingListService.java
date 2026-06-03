package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public List<WaitingListResult> getWaitingListByName(String name) {
        return waitingListRepository.findByName(name).stream()
                .map(row -> WaitingListResult.from(row.waitingList(), row.waitingOrder()))
                .toList();
    }

    @Transactional
    public WaitingListResult create(final WaitingListCreateCommand createCommand) {
        Theme findTheme = findThemeOrThrow(createCommand.themeId());
        ReservationTime findReservationTime = findReservationTimeOrThrow(createCommand.timeId());

        WaitingList waitingList = WaitingList.create(createCommand.name(), createCommand.date(), findTheme, findReservationTime);

        validateWaitingList(waitingList, findReservationTime, findTheme);

        try {
            WaitingList savedWaitingList = waitingListRepository.save(waitingList);
            int waitingOrder = waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(savedWaitingList);
            return WaitingListResult.from(savedWaitingList, waitingOrder);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }

    @Transactional
    public void delete(final WaitingListDeleteCommand deleteCommand) {
        WaitingList findWaitingList = waitingListRepository.findById(deleteCommand.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));
        
        if (!findWaitingList.getName().equals(deleteCommand.name())) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }

        findWaitingList.validateNotPast();
        
        waitingListRepository.deleteById(deleteCommand.waitingListId());
    }

    private ReservationTime findReservationTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme findThemeOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateWaitingList(WaitingList waitingList, ReservationTime findReservationTime, Theme findTheme) {
        waitingList.validateNotPast();

        validateReservationExists(waitingList, findReservationTime, findTheme);
        validateNotDuplicated(waitingList, findTheme, findReservationTime);
    }

    private void validateReservationExists(WaitingList waitingList, ReservationTime findReservationTime, Theme findTheme) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                waitingList.getReservationDate().getDate(), findReservationTime.getId(), findTheme.getId())) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }
    }

    private void validateNotDuplicated(WaitingList waitingList, Theme findTheme, ReservationTime findReservationTime) {
        if (waitingListRepository.existsByNameAndThemeAndDateAndTime(
                waitingList.getName(), findTheme.getId(), waitingList.getReservationDate().getDate(), findReservationTime.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }
}
