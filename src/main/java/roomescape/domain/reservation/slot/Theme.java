package roomescape.domain.reservation.slot;

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

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = ensureNameIsValid(name);
        this.description = Objects.requireNonNull(description,"테마 설명이 없습니다.");
        this.thumbnail = Objects.requireNonNull(thumbnail,"테마 이미지가 없습니다.");
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    protected Theme() {
    }

    private String ensureNameIsValid(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("테마의 이름은 1글자 이상으로 이루어져야 합니다.");
        }
        return name;
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
