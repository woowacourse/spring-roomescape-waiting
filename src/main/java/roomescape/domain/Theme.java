package roomescape.domain;

import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImageUrl;
    private final int price;
    private boolean isActive;

    public Theme(Long id, String name, String description, String thumbnailImageUrl, boolean isActive) {
        this(id, name, description, thumbnailImageUrl, 0, isActive);
    }

    public Theme(Long id, String name, String description, String thumbnailImageUrl, int price, boolean isActive) {
        validateTheme(name, description, thumbnailImageUrl, price);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.price = price;
        this.isActive = isActive;
    }

    public Theme(String name, String description, String thumbnailImageUrl) {
        this(name, description, thumbnailImageUrl, 0);
    }

    public Theme(String name, String description, String thumbnailImageUrl, int price) {
        this(null, name, description, thumbnailImageUrl, price, true);
    }

    private static void validateTheme(String name, String description, String thumbnailImageUrl, int price) {
        validateName(name);
        validateDescription(description);
        validateThumbnailImageUrl(thumbnailImageUrl);
        validatePrice(price);
    }

    private static void validatePrice(int price) {
        if (price < 0) {
            throw new RoomEscapeException("가격은 0원 이상이어야 합니다.");
        }
    }

    private static void validateThumbnailImageUrl(String thumbnailImageUrl) {
        if (isInvalidImageUrl(thumbnailImageUrl)) {
            throw new RoomEscapeException("올바른 이미지 주소 형식이 아닙니다.");
        }
    }

    private static boolean isInvalidImageUrl(String thumbnailImageUrl) {
        try {
            URI uri = URI.create(thumbnailImageUrl);
            return uri.getScheme() == null || !uri.getScheme().startsWith("http");
        } catch (Exception e) {
            throw new RoomEscapeException("올바른 이미지 주소 형식이 아닙니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new RoomEscapeException("설명은 필수 값입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomEscapeException("이름은 필수 값입니다.");
        }
    }

    public void deactivate() {
        if (!isActive) {
            throw new RoomEscapeException("이미 비활성화 된 테마입니다.");
        }
        this.isActive = false;
    }

    public void activate() {
        if (isActive) {
            throw new RoomEscapeException("이미 활성화 된 테마입니다.");
        }
        this.isActive = true;
    }
}
