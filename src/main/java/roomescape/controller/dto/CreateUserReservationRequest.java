package roomescape.controller.dto;

import roomescape.system.exception.RoomescapeException;

public record CreateUserReservationRequest(String date, Long themeId, Long timeId) {

    public CreateUserReservationRequest {
        validate(date, timeId, themeId);
    }

    private void validate(String date, Long timeId, Long themeId) {
        if (date == null || date.isBlank() || timeId == null || themeId == null) {
            throw new RoomescapeException("요청 필드는 비어있을 수 없습니다.");
        }
    }
}
