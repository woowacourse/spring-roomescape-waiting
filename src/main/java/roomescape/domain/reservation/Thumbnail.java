package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;

@Embeddable
public record Thumbnail(String thumbnail) {
    public Thumbnail {
        validate(thumbnail);
    }

    private void validate(final String url) {
        validateNull(url);
    }

    private void validateNull(final String url) {
        if (url.isBlank()) {
            throw new IllegalArgumentException("경로는 공백일 수 없습니다!");
        }
    }

    public String asString() {
        return thumbnail;
    }
}
