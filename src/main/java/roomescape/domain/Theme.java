package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import org.hibernate.proxy.HibernateProxy;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

@Entity
@Table(name = "theme")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "name")
    private String name;
    @Column(nullable = false, name = "description")
    private String description;
    @Column(nullable = false, name = "thumbnail")
    private String thumbnail;

    protected Theme() {
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateEmpty("name", name);
        validateEmpty("description", description);
        validateEmpty("thumbnail", thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
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
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        final Theme theme = (Theme) o;
        return getId() != null && Objects.equals(getId(), theme.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
