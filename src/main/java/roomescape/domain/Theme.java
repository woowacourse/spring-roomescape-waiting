package roomescape.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private ThemeName name;

    private String description;

    private String thumbnail;

    public Theme() {
    }

    public Theme(Long id, ThemeName name, String description, String thumbnail) {
        validateBlank(description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        this(id, new ThemeName(name), description, thumbnail);
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, new ThemeName(name), description, thumbnail);
    }

    private void validateBlank(String description, String thumbnail) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("테마 설명은 필수로 입력해야 합니다.");
        }

        if (thumbnail == null || thumbnail.isBlank()) {
            throw new IllegalArgumentException("테마 썸네일은 필수로 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
