package roomescape.domain;

import java.util.Objects;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;
    private final Long price;

    public Theme(Long id, String name, String description, String thumbnailUrl, Long price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public Theme(String name, String description, String thumbnailUrl, Long price) {
        this.id = null;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public static Theme of(Long id, Theme theme) {
        return new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.getPrice());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
