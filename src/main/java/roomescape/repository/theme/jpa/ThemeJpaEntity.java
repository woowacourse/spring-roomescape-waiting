package roomescape.repository.theme.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import roomescape.domain.theme.Theme;

@Entity
@Table(name = "theme")
public class ThemeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    protected ThemeJpaEntity() {
    }

    private ThemeJpaEntity(
            final Long id,
            final String name,
            final String description,
            final String thumbnailUrl
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ThemeJpaEntity from(final Theme theme) {
        return new ThemeJpaEntity(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }

    public Theme toDomain() {
        return Theme.of(id, name, description, thumbnailUrl);
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
