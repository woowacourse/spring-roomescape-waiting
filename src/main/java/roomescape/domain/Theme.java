package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Theme extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    public Theme(Long id,
                 String name,
                 String description,
                 String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(String name,
                 String description,
                 String thumbnail) {
        this(null, name, description, thumbnail);
    }

    protected Theme() {
    }

    private void validate(String name,
                          String description,
                          String thumbnail) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마 이름을 선택해주세요");
        }
        if (description == null) {
            throw new IllegalArgumentException("테마 설명이 존재하지 않습니다.");
        }
        if (thumbnail == null) {
            throw new IllegalArgumentException("테마 썸네일이 존재하지 않습니다.");
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
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
        return Objects.equals(id, theme.id) &&
               Objects.equals(name, theme.name) &&
               Objects.equals(description, theme.description) &&
               Objects.equals(thumbnail, theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
