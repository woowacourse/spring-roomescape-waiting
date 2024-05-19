package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "theme")
public class Theme {
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_THUMBNAIL_URL_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ThemeName name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String thumbnailUrl;

    protected Theme() {
    }

    public Theme(Long id, ThemeName name, String description, String thumbnailUrl) {
        validateDescription(description);
        validateThumbnailUrl(thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Theme(String name, String description, String thumbnailUrl) {
        this(null, new ThemeName(name), description, thumbnailUrl);
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 설명은 %d자 이하여야 합니다.", MAX_DESCRIPTION_LENGTH));
        }
    }

    private void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl != null && thumbnailUrl.length() > MAX_THUMBNAIL_URL_LENGTH) {
            throw new IllegalArgumentException(String.format("테마 썸네일 URL은 %d자 이하여야 합니다.", MAX_THUMBNAIL_URL_LENGTH));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Theme other)) {
            return false;
        }
        return Objects.equals(id, other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
