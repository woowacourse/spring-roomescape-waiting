package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Entity
public class Theme {
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ThemeName name;
    private String description;
    private String thumbnailUrl;

    public Theme() {
    }

    public Theme(ThemeName name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, ThemeName name, String description, String thumbnailUrl) {
        int descriptionLength = description.length();
        if (descriptionLength > MAX_DESCRIPTION_LENGTH) {
            throw new RoomescapeException(
                    RoomescapeErrorCode.BAD_REQUEST, String.format("테마 설명은 %s자 이하만 가능합니다.", MAX_DESCRIPTION_LENGTH));
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "테마 썸네일은 비어있을 수 없습니다.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Theme withId(long id) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.asText();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
