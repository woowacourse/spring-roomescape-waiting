package roomescape.domain.dto;

import roomescape.exception.clienterror.EmptyValueNotAllowedException;
import roomescape.exception.clienterror.InvalidIdException;

import java.time.LocalDate;

public record ReservationRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {

    public ReservationRequest {
        isValid(date, timeId, themeId, memberId);
    }

    public ReservationRequest with(Long memberId) {
        return new ReservationRequest(date, timeId, themeId, memberId);
    }

    private void isValid(final LocalDate date, final Long timeId, final Long themeId, final Long memberId) {
        if (date == null) {
            throw new EmptyValueNotAllowedException("date");
        }

        if (timeId == null) {
            throw new EmptyValueNotAllowedException("timeId");
        }

        if (timeId <= 0) {
            throw new InvalidIdException("timeId", timeId);
        }

        if (themeId == null) {
            throw new EmptyValueNotAllowedException("themeId");
        }

        if (themeId <= 0) {
            throw new InvalidIdException("themeId", themeId);
        }
    }
}
