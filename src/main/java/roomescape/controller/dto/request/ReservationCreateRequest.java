package roomescape.controller.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.custom.InvalidRequestArgumentException;

public record ReservationCreateRequest(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationCreateRequest {
        validate(name, date, timeId, themeId);
    }

    public Reservation toReservation(ReservationTime reservationTime, Theme theme) {
        return new Reservation(name, new Slot(date, reservationTime, theme));
    }

    public Wait toWait(LocalDateTime createdAt, ReservationTime reservationTime, Theme theme) {
        return new Wait(createdAt, name, new Slot(date, reservationTime, theme));
    }

    private void validate(String name, LocalDate date, Long timeId, Long themeId) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestArgumentException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (date == null) {
            throw new InvalidRequestArgumentException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (timeId == null) {
            throw new InvalidRequestArgumentException("예약 시간은 비어 있을 수 없습니다.");
        }
        if (themeId == null) {
            throw new InvalidRequestArgumentException("테마는 비어 있을 수 없습니다.");
        }
    }
}
