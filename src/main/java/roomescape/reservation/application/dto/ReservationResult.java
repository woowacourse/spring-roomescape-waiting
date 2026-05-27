package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.theme.application.dto.ThemeResult;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ThemeResult theme,
        ReservationTimeResult time,
        Status status,
        Long rank
) {

    public static ReservationResult confirmed(Reservation reservation, ThemeResult themeResult,
                                              ReservationTimeResult timeResult) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getSlot().date(),
                themeResult,
                timeResult,
                Status.CONFIRM,
                null
        );
    }

    public static ReservationResult waiting(Waiting waiting, ThemeResult themeResult,
                                            ReservationTimeResult timeResult,
                                            Long rank) {
        return new ReservationResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getSlot().date(),
                themeResult,
                timeResult,
                Status.WAITING,
                rank
        );
    }

    public static ReservationResult from(ReservationDetail reservationDetail) {
        return new ReservationResult(
                reservationDetail.reservationId(),
                reservationDetail.username(),
                reservationDetail.date(),
                ThemeResult.from(
                        reservationDetail.themeId(),
                        reservationDetail.themeName(),
                        reservationDetail.themeDescription(),
                        reservationDetail.thumbnailImgUrl()
                ),
                ReservationTimeResult.from(
                        reservationDetail.timeId(),
                        reservationDetail.startAt()
                ),
                Status.WAITING, // TODO: 이건 나중에 조회한게 예약인지, 대기인지 구분하는 로직이 추가 필요 반드시 수정 ✅
                null // TODO: 이건 나중에 조회한게 예약인지, 대기인지 구분하는 로직이 추가 후 수정 ✅
        );
    }

    public enum Status {
        CONFIRM, WAITING
    }
}
