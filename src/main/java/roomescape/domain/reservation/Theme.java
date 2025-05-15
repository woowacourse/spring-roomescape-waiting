package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.domain.BusinessRuleViolationException;

@Entity
public class Theme {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final int MAX_THUMBNAIL_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new BusinessRuleViolationException("테마 이름은 비어있을 수 없습니다.");
        }
        if (MAX_NAME_LENGTH < name.length()) {
            throw new BusinessRuleViolationException("테마 이름은 %d자 이하여야 합니다.".formatted(MAX_NAME_LENGTH));
        }
    }

    protected Theme() {
    }

    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new BusinessRuleViolationException("테마 설명은 비어있을 수 없습니다.");
        }
        if (MAX_DESCRIPTION_LENGTH < description.length()) {
            throw new BusinessRuleViolationException("테마 설명은 %d자 이하여야 합니다.".formatted(MAX_DESCRIPTION_LENGTH));
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail.isBlank()) {
            throw new BusinessRuleViolationException("테마 썸네일은 비어있을 수 없습니다.");
        }
        if (MAX_THUMBNAIL_LENGTH < thumbnail.length()) {
            throw new BusinessRuleViolationException("테마 썸네일은 %d자 이하여야 합니다.".formatted(MAX_THUMBNAIL_LENGTH));
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

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id)
                && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description)
                && Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
