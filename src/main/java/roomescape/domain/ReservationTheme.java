package roomescape.domain;

public class ReservationTheme {

    private final long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public ReservationTheme(final long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public ReservationTheme(final String name, final String description, final String thumbnail) {
        this.id = 0L;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public ReservationTheme toEntity(long id) {
        return new ReservationTheme(id, name, description, thumbnail);
    }

    public long getId() {
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
