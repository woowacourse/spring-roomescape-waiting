package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.domain.exception.InvalidDomainException;

@Entity
public class Theme {

    private static final int MAX_NAME_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false)
    private String description;

    // 컬럼명은 CamelCaseToUnderscoresNamingStrategy 에 위임: thumbnailUrl -> thumbnail_url (관찰로 검증)
    @Column(nullable = false)
    private String thumbnailUrl;

    protected Theme() {
    }

    private Theme(Long id, String name, String description, String thumbnailUrl) {
        validate(name, description, thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Theme create(String name, String description, String thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public static Theme withId(Long id, String name, String description, String thumbnailUrl) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    private void validate(String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("테마 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                    "테마 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidDomainException("테마 설명은 비어 있을 수 없습니다.");
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null || thumbnail.isBlank()) {
            throw new InvalidDomainException("테마 썸네일은 비어 있을 수 없습니다.");
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
