package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.BadRequestException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "theme")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column
    private String description;
    @Column
    private String thumbnail;

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme createWithPrimaryKey(Theme theme, Long newPrimaryKey) {
        return new Theme(newPrimaryKey, theme.name, theme.description, theme.thumbnail);
    }

    private void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("테마 이름은 비워둘 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Theme theme)) {
            return false;
        }
        return Objects.equals(id, theme.id) && Objects.equals(name, theme.name)
            && Objects.equals(description, theme.description) && Objects.equals(thumbnail,
            theme.thumbnail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
