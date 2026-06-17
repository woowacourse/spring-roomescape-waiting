package roomescape.theme.domain;

import lombok.Getter;

@Getter
public class Theme {
    public static final int DEFAULT_PRICE = 10_000;

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final int price;

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        this(id, name, description, thumbnailUrl, DEFAULT_PRICE);
    }

    public Theme(Long id, String name, String description, String thumbnailUrl, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }
}
