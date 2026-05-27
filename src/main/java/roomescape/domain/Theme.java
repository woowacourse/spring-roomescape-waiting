package roomescape.domain;

import java.util.Objects;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailImageUrl;

    public Theme(
            Long id,
            String name,
            String description,
            String thumbnailImageUrl
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public Theme(
            String name,
            String description,
            String thumbnailImageUrl
    ) {
        this(null, name, description, thumbnailImageUrl);
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

    public String getThumbnailImageUrl() {
        return thumbnailImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(getId(), theme.getId()) && Objects.equals(getName(), theme.getName()) && Objects.equals(
                getDescription(), theme.getDescription()) && Objects.equals(getThumbnailImageUrl(),
                theme.getThumbnailImageUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getThumbnailImageUrl());
    }
}
