package roomescape.theme.domain;

public class Theme {

    private static final long DEFAULT_PRICE = 10000L;

    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final Long price;

    private Theme(Long id, String name, String description, String imageUrl, Long price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("테마 이미지 URL은 필수입니다.");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("테마 가격은 양수여야 합니다.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public static Theme of(String name, String description, String imageUrl) {
        return new Theme(null, name, description, imageUrl, DEFAULT_PRICE);
    }

    public static Theme of(String name, String description, String imageUrl, Long price) {
        return new Theme(null, name, description, imageUrl, price);
    }

    public static Theme restore(Long id, String name, String description, String imageUrl) {
        return new Theme(id, name, description, imageUrl, DEFAULT_PRICE);
    }

    public static Theme restore(Long id, String name, String description, String imageUrl, Long price) {
        return new Theme(id, name, description, imageUrl, price);
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

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getPrice() {
        return price;
    }
}