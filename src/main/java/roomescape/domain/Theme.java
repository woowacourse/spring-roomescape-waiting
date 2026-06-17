package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "thumbnail_image_url", nullable = false)
    private String thumbnailImageUrl;

    protected Theme() {
    }

    public Theme(
            Long id,
            String name,
            String description,
            String thumbnailImageUrl
    ) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.thumbnailImageUrl = Objects.requireNonNull(thumbnailImageUrl);
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
