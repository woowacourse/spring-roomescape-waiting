package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class Theme {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id", nullable = false))
    private ThemeId id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String thumbnail;

    protected Theme() {}

    public Theme(
            final Long id,
            final String name,
            final String description,
            final String thumbnail
    ) {
        this.id = new ThemeId(id);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        validateTheme();
    }

    public Theme(
            final String name,
            final String description,
            final String thumbnail
    ) {
        this(null, name, description, thumbnail);
    }

    private Theme(final Builder builder) {
        this(builder.id, builder.name, builder.description, builder.thumbnail);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void validateTheme() {
        if (name == null || description == null || thumbnail == null) {
            throw new IllegalArgumentException("Theme field cannot be null");
        }
    }

    public Long getId() {
        return id.getValue();
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

    public static class Builder {

        private Long id;
        private String name;
        private String description;
        private String thumbnail;

        public Builder id(final Long id) {
            this.id = id;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder thumbnail(final String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Theme build() {
            return new Theme(this);
        }
    }
}
