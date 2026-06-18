package roomescape.theme.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "theme")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;

    private ThemeEntity(final String name, final String description, final String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ThemeEntity from(final Theme theme) {
        return new ThemeEntity(theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }

    public Theme toDomain() {
        return Theme.of(id, name, description, thumbnailUrl);
    }
}
