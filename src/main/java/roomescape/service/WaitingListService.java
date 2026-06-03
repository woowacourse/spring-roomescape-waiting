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

    @Transactional
    public WaitingListResult create(final WaitingListCreateCommand createCommand) {
        final Theme findTheme = findThemeOrThrow(createCommand.themeId());
        final ReservationTime findReservationTime = findReservationTimeOrThrow(createCommand.timeId());

        final WaitingList waitingList = WaitingList.create(createCommand.name(), createCommand.date(), findTheme, findReservationTime);

        validateWaitingList(waitingList, findReservationTime, findTheme);

        try {
            final WaitingList savedWaitingList = waitingListRepository.save(waitingList);
            final int waitingOrder = waitingListRepository.findWaitingOrderByDateAndTimeIdAndThemeId(savedWaitingList);
            return WaitingListResult.from(savedWaitingList, waitingOrder);
        } catch (DataAccessException e) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }

    public List<WaitingListResult> getWaitingListByName(final String name) {
        return waitingListRepository.findByName(name).stream()
                .map(row -> WaitingListResult.from(row.waitingList(), row.waitingOrder()))
                .toList();
    }

    @Transactional
    public void delete(final WaitingListDeleteCommand deleteCommand) {
        final WaitingList findWaitingList = waitingListRepository.findById(deleteCommand.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));
        
        if (!findWaitingList.getName().equals(deleteCommand.name())) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }

        findWaitingList.validateNotPast();
        
        waitingListRepository.deleteById(deleteCommand.waitingListId());
    }

    private ReservationTime findReservationTimeOrThrow(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme findThemeOrThrow(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateWaitingList(final WaitingList waitingList, final ReservationTime findReservationTime, final Theme findTheme) {
        waitingList.validateNotPast();

        validateReservationExists(waitingList, findReservationTime, findTheme);
        validateNotDuplicated(waitingList, findTheme, findReservationTime);
    }

    private void validateReservationExists(final WaitingList waitingList, final ReservationTime findReservationTime, final Theme findTheme) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                waitingList.getReservationDate().getDate(), findReservationTime.getId(), findTheme.getId())) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }
    }

    private void validateNotDuplicated(final WaitingList waitingList, final Theme findTheme, final ReservationTime findReservationTime) {
        if (waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(
                waitingList.getName(), waitingList.getReservationDate().getDate(), findReservationTime.getId(), findTheme.getId())) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }
}
