package roomescape.domain;

import java.net.URI;
import java.util.Objects;
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
    private final Long price;
    private boolean isActive;

    private Theme(Long id, String name, String description, String thumbnailImageUrl, Long price, boolean isActive) {
        validateTheme(name, description, thumbnailImageUrl, price);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.price = price;
        this.isActive = isActive;
    }

    public static Theme create(String name, String description, String thumbnailImageUrl, Long price) {
        return new Theme(null, name, description, thumbnailImageUrl, price, true);
    }

    public static Theme restore(Long id, String name, String description, String thumbnailImageUrl, Long price,
                                boolean isActive) {
        return new Theme(Objects.requireNonNull(id, "복원 시 id 값은 필수입니다"), name, description, thumbnailImageUrl, price,
                isActive);
    }

    private static void validateTheme(String name, String description, String thumbnailImageUrl, Long price) {
        validateName(name);
        validateDescription(description);
        validateThumbnailImageUrl(thumbnailImageUrl);
        validatePrice(price);
    }

    private static void validatePrice(Long price) {
        if (price == null) {
            throw new RoomEscapeException("금액은 필수 값입니다.");
        }
        if (price <= 0) {
            throw new RoomEscapeException("금액은 양수여야 합니다.");
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

    public void validatePaymentAmount(Long amount) {
        if (!price.equals(amount)) {
            throw new RoomEscapeException(String.format("결제 금액이 테마 금액과 일치하지 않습니다. (테마 금액: %d)", price));
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
