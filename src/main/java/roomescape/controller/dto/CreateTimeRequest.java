package roomescape.controller.dto;

public record CreateTimeRequest(String startAt) {

    public CreateTimeRequest {
        validate(startAt);
    }

    private void validate(String startAt) {
        if (startAt.isBlank()) {
            throw new IllegalArgumentException("요청 필드는 비어있을 수 없습니다.");
        }
    }
}
