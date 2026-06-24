package roomescape.domain.theme;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final Long price;

    public Theme(Long id, String name, String description, String thumbnailUrl, Long price) {
        validateNullOrBlank(name, description, thumbnailUrl);
        validatePrice(price);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public Theme(String name, String description, String thumbnailUrl, Long price) {
        this(null, name, description, thumbnailUrl, price);
    }

    private void validateNullOrBlank(String name, String description, String thumbnailUrl) {
        validateName(name);
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
    }

    private void validatePrice(Long price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
    }

    private void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new IllegalArgumentException("썸네일 URL은 필수입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Long getPrice() {
        return price;
    }
}
