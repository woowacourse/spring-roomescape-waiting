package roomescape.theme.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import roomescape.exception.BadRequestException;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(nullable = false, unique = true, name = "name")
    private String name;
    @Column(nullable = false, name = "description")
    private String description;
    @Column(nullable = false, name = "thumbnail")
    private String thumbnail;

    protected Theme() {
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateNotNull(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateNotNull(String name, String description, String thumbnail) {
        try {
            Objects.requireNonNull(name, "테마 이름이 작성되지 않았습니다.");
            Objects.requireNonNull(description, "테마 설명이 작성되지 않았습니다.");
            Objects.requireNonNull(thumbnail, "테마 썸네일이 선택되지 않았습니다.");
        } catch (NullPointerException e) {
            throw new BadRequestException(e.getMessage());
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme theme)) return false;

        if (id == null || theme.id == null) {
            return Objects.equals(name, theme.name);
        }
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(name);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Theme{" +
               "id=" + id +
               ", name='" + name +
               '}';
    }
}
