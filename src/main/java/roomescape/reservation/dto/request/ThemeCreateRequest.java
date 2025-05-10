package roomescape.reservation.dto.request;

import roomescape.global.exception.InvalidInputException;

public record ThemeCreateRequest(String name, String description, String thumbnail) {

    public ThemeCreateRequest {
        validateNull(name, description, thumbnail);
        validateLength(name, description, thumbnail);
    }

    private void validateNull(String name, String description, String thumbnail) {
        if (name == null) {
            throw new InvalidInputException("예약할 날짜가 입력되지 않았다.");
        }
        if (description == null) {
            throw new InvalidInputException("예약자 이름이 입력되지 않았다.");
        }
        if (thumbnail == null) {
            throw new InvalidInputException("예약할 시간이 입력되지 않았다.");
        }
    }

    private void validateLength(String name, String description, String thumbnail) {
        if (name.isBlank()) {
            throw new InvalidInputException("이름은 한 글자 이상이어야 한다.");
        }
        if (description.isBlank()) {
            throw new InvalidInputException("테마 설명은 한 글자 이상이어야 한다.");
        }
        if (thumbnail.isBlank()) {
            throw new InvalidInputException("테마 썸네일 URL은 한 글자 이상이어야 한다.");
        }
    }
}
