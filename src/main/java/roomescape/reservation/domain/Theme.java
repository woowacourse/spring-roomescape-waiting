package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.global.exception.ViolationException;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
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

    public Theme(Long id, Theme theme) {
        this(id, theme.name, theme.description, theme.thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ViolationException("테마 이름은 비어있을 수 없습니다.");
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
