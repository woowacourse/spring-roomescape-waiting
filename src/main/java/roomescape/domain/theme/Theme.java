package roomescape.domain.theme;

public class Theme {
    private final Long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final long price;

    private Theme(Long id, String name, String description, String imageUrl, long price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public static Theme of(Long id, String name, String description, String imageUrl, long price) {
        return new Theme(id, name, description, imageUrl, price);
    }

    public static Theme of(String name, String description, String imageUrl, long price) {
        return new Theme(null, name, description, imageUrl, price);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public long getPrice() { return price; }
}
