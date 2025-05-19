package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String thumbnail;

    protected Theme() {}

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        validateTheme();
    }

    public Theme(final String name, final String description, final String thumbnail) {
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
