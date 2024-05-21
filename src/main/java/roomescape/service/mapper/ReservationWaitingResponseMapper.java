package roomescape.service.mapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import roomescape.domain.BaseEntity;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.dto.ThemeResponse;

public class ReservationWaitingResponseMapper {
    public static ReservationWaitingResponse toResponseWithoutPriority(ReservationWaiting reservationWaiting) {
        Reservation reservation = reservationWaiting.getReservation();

        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse timeResponse = ReservationTimeResponseMapper.toResponse(reservationTime);

        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = ThemeResponseMapper.toResponse(theme);

        String waitingMemberName = reservationWaiting.getWaitingMember().getName();

        return new ReservationWaitingResponse(reservationWaiting.getId(), waitingMemberName,
                reservation.getDate(), timeResponse, themeResponse, null);
    }

    public static ReservationWaitingResponse toResponse(ReservationWaiting target, List<ReservationWaiting> all) {
        Reservation reservation = target.getReservation();

        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse timeResponse = ReservationTimeResponseMapper.toResponse(reservationTime);

        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = ThemeResponseMapper.toResponse(theme);

        String waitingMemberName = target.getWaitingMember().getName();

        int priority = calculatePriority(target, all);

        return new ReservationWaitingResponse(target.getId(), waitingMemberName,
                reservation.getDate(), timeResponse, themeResponse, priority);
    }

    private static int calculatePriority(ReservationWaiting target, List<ReservationWaiting> all) {
        int priority = 0;
        List<ReservationWaiting> copyOfAll = new ArrayList<>(all);
        copyOfAll.sort(Comparator.comparing(BaseEntity::getCreateAt));
        for (int i = 0; i < copyOfAll.size(); i++) {
            if (target.equals(copyOfAll.get(i))) {
                priority = i + 1;
            }
        }
        if (priority == 0) {
            throw new IllegalArgumentException("순위를 판별할 대상이 목록에 없습니다.");
        }
        return priority;
    }
}
