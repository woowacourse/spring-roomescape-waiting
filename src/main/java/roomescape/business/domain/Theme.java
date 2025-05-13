package roomescape.business.domain;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);

        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id) {
        this.id = id;
        this.name = null;
        this.description = null;
        this.thumbnail = null;
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name 필드가 비어있습니다.");
        }
    }

    private void validateDescription(final String description) {
        if (description == null) {
            throw new IllegalArgumentException("description 필드가 null 입니다.");
        }
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null) {
            throw new IllegalArgumentException("thumbnail 필드가 null 입니다.");
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
