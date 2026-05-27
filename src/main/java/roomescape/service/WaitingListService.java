package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

@RequiredArgsConstructor
@Service
public class WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingListResult create(WaitingListCreateCommand createCommand) {
        Theme findTheme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
        ReservationTime findReservationTime = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));

        WaitingList waitingList = WaitingList.create(createCommand.date(), createCommand.name(), findTheme, findReservationTime);
        WaitingList savedWaitingList = waitingListRepository.save(waitingList);
        return WaitingListResult.from(savedWaitingList);
    }
}
