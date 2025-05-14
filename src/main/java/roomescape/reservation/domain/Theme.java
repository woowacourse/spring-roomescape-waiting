package roomescape.reservation.domain;

public class Theme {

    private static final long EMPTY_ID = 0L;

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        validateTheme();
    }

    public static Theme withoutId(final String name, final String description, final String thumbnail) {
        return new Theme(EMPTY_ID, name, description, thumbnail);
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

    public boolean existId() {
        return id != EMPTY_ID;
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

        private Long id = EMPTY_ID;
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
