package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.exception.InvalidInputException;

@Embeddable
public record Thumbnail(
    @Column(name = "thumbnail", nullable = false)
    String url
) {

    public Thumbnail {
        if (url == null || url.isBlank() || url.contains(" ")) {
            throw new InvalidInputException("썸네일 URL은 공백이거나 공백을 포함할 수 없습니다.");
        }
    }
}
