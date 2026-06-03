package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record WaitingResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        Status status,
        int rank
) {

    public static WaitingResult from(Waiting waiting, ThemeResult themeResult,
                                     ReservationTimeResult timeResult) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getUserName(),
                waiting.getSlot().date(),
                themeResult,
                timeResult,
                Status.WAITING,
                waiting.getRank().value()
        );
    }

    public static WaitingResult from(WaitingDetail waitingDetail) {
        return new WaitingResult(
                waitingDetail.waitingId(),
                waitingDetail.username(),
                waitingDetail.date(),
                ThemeResult.from(
                        waitingDetail.themeId(),
                        waitingDetail.themeName(),
                        waitingDetail.themeDescription(),
                        waitingDetail.thumbnailImgUrl()
                ),
                ReservationTimeResult.from(
                        waitingDetail.timeId(),
                        waitingDetail.startAt()
                ),
                Status.WAITING,
                waitingDetail.rank()
        );
    }

    public enum Status {
        WAITING
    }
}
