package roomescape.theme.domain;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final int price;

    private Theme(Long id, String name, String description, String imageUrl, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public static Theme restore(Long id, String name, String description, String imageUrl, int price) {
        return new Theme(id, name, description, imageUrl, price);
    }

    public static Theme of(String name, String description, String imageUrl, int price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름은 필수입니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수입니다.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("테마 이미지 URL은 필수입니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("테마 가격은 0보다 커야 합니다.");
        }
        return new Theme(null, name, description, imageUrl, price);
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

    public int getPrice() {
        return price;
    }
}