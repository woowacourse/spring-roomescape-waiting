package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    protected Theme() {
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateThemeContents(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateThemeContents(String name, String description, String thumbnail) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("테마명이 입력되지 않았습니다.");
        }
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("테마 설명이 입력되지 않았습니다.");
        }
        if (thumbnail == null || thumbnail.isEmpty()) {
            throw new IllegalArgumentException("썸네일이 입려되지 않았습니다.");
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

}
