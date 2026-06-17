package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.request.WaitingListCreateRequest;
import roomescape.dto.command.WaitingListDeleteCommand;
import roomescape.dto.response.WaitingListResult;
import roomescape.exception.BusinessException;
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

    @Transactional
    public WaitingListResult create(final WaitingListCreateRequest command) {
        final Theme findTheme = findThemeOrThrow(command.themeId());
        final ReservationTime findReservationTime = findReservationTimeOrThrow(command.timeId());

        final WaitingList waitingList = WaitingList.create(command.name(), command.date(), findTheme, findReservationTime);

        validateWaitingList(waitingList);

        final WaitingList savedWaitingList = waitingListRepository.save(waitingList);
        final int waitingOrder = waitingListRepository.findWaitingOrderByDateAndTimeIdAndThemeId(savedWaitingList);
        return WaitingListResult.from(savedWaitingList, waitingOrder);
    }

    public List<WaitingListResult> getWaitingListByName(final String name) {
        return waitingListRepository.findByName(name).stream()
                .map(row -> WaitingListResult.from(row.waitingList(), row.waitingOrder()))
                .toList();
    }

    @Transactional
    public void delete(final WaitingListDeleteCommand command) {
        final WaitingList findWaitingList = waitingListRepository.findById(command.waitingListId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WAITING_LIST_NOT_FOUND));

        findWaitingList.validateForDeletion(command.name());

        waitingListRepository.deleteById(command.waitingListId());
    }

    private ReservationTime findReservationTimeOrThrow(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme findThemeOrThrow(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    private void validateWaitingList(final WaitingList waitingList) {
        waitingList.validateNotPast();
        validateReservationExists(waitingList);
        validateNotDuplicated(waitingList);
    }

    private void validateReservationExists(final WaitingList waitingList) {
        if (reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                waitingList.getName(), waitingList.getReservationDate().getDate(), waitingList.getReservationTime().getId(), waitingList.getTheme().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_BY_SELF);
        }

        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(
                waitingList.getReservationDate().getDate(), waitingList.getReservationTime().getId(), waitingList.getTheme().getId())) {
            throw new BusinessException(ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }
    }

    private void validateNotDuplicated(final WaitingList waitingList) {
        if (waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(
                waitingList.getName(), waitingList.getReservationDate().getDate(), waitingList.getReservationTime().getId(), waitingList.getTheme().getId())) {
            throw new BusinessException(ErrorCode.ALREADY_ON_WAITING_LIST);
        }
    }
}
