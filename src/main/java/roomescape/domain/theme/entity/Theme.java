package roomescape.domain.theme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "theme",
    indexes = @Index(
        name = "uq_active_theme",
        columnList = "active_name",
        unique = true
    )
)
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @NotBlank
    @Column(name = "image_url", nullable = false, length = 2000)
    private String imageUrl;

    @Column(name = "deleted_at", columnDefinition = "DATETIME DEFAULT NULL")
    private LocalDateTime deletedAt;

    @Column(
        name = "active_name",
        insertable = false,
        updatable = false,
        columnDefinition = "VARCHAR(255) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN name ELSE NULL END)"
    )
    private String activeName;

    private Theme(Long id, String name, String description, String imageUrl, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.deletedAt = deletedAt;
    }

    public Theme() {

    }

    public static Theme create(String name, String description, String imageUrl) {
        return new Theme(null, name, description, imageUrl, null);
    }

    public static Theme reconstruct(Long id, String name, String description, String imageUrl,
        LocalDateTime deletedAt) {
        return new Theme(id, name, description, imageUrl, deletedAt);
    }

    public boolean isSameId(Theme theme) {
        return id.equals(theme.id);
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

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
