package roomescape.dto.request;

import roomescape.exception.custom.InvalidInputException;

public record ThemeRequest(String name, String description, String thumbnail) {

    public ThemeRequest {
        validateNull(name, description, thumbnail);
        validateLengthOfString(name, description, thumbnail);
    }

    private void validateNull(String name, String description, String thumbnail) {
        if (name == null || description == null || thumbnail == null) {
            throw new InvalidInputException("선택되지 않은 값 존재");
        }
    }

    private void validateLengthOfString(String name, String description, String thumbnail) {
        if (name.isBlank() || description.isBlank() || thumbnail.isBlank()) {
            throw new InvalidInputException("입력되지 않은 값 존재");
        }
    }
}
