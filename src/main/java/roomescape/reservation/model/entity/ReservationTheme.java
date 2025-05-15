package roomescape.reservation.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "theme")
public class ReservationTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    @Builder
    public ReservationTheme(Long id, String name, String description, String thumbnail) {
        validateNotBlank(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public ReservationTheme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public ReservationTheme assignId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("할당할 id는 null이 될 수 없습니다.");
        }
        return new ReservationTheme(id, name, description, thumbnail);
    }

    private void validateNotBlank(String name, String description, String thumbnail) {
        if (name == null) {
            throw new IllegalArgumentException("테마명은 null이 될 수 없습니다.");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("테마명은 비어 있을 수 없습니다.");
        }
        if (description == null) {
            throw new IllegalArgumentException("테마설명은 null이 될 수 없습니다.");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("테마설명은 비어 있을 수 없습니다.");
        }
        if (thumbnail == null) {
            throw new IllegalArgumentException("테마 썸네일은 null이 될 수 없습니다.");
        }
        if (thumbnail.isBlank()) {
            throw new IllegalArgumentException("테마 썸네일은 비어 있을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTheme that = (ReservationTheme) o;
        if (this.id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
