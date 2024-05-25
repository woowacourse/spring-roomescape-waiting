package roomescape.domain.reservation.detail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.domain.exception.DomainValidationException;
import roomescape.domain.member.Member;

@Entity
public class Theme {

    private static final int NAME_MAX_LENGTH = 30;
    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int THUMBNAIL_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    protected Theme() {
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name, description, thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validate(String name, String description, String thumbnail) {
        validateNAme(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateNAme(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("이름은 필수 값입니다.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new DomainValidationException(String.format("이름은 %d자를 넘을 수 없습니다.", NAME_MAX_LENGTH));
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new DomainValidationException("설명은 필수 값입니다.");
        }

        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException(String.format("설명은 %d자를 넘을 수 없습니다.", DESCRIPTION_MAX_LENGTH));
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new DomainValidationException("이미지는 필수 값입니다.");
        }

        if (thumbnail.length() > THUMBNAIL_MAX_LENGTH) {
            throw new DomainValidationException(String.format("이미지는 %d자를 넘을 수 없습니다.", THUMBNAIL_MAX_LENGTH));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Theme theme)) {
            return false;
        }

        return this.getId() != null && Objects.equals(getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
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
}
