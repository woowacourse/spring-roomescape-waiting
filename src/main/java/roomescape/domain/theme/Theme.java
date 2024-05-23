package roomescape.domain.theme;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import roomescape.domain.reservation.Reservation;
import roomescape.system.exception.RoomescapeException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Description description;

    @Embedded
    private Thumbnail thumbnail;

    @OneToMany(mappedBy = "theme")
    private List<Reservation> reservations = new ArrayList<>();

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = new Name(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    protected Theme() {
    }

    public void validateDuplication(List<Theme> others) {
        if (others.stream()
            .anyMatch(other -> name.equals(other.name))) {
            throw new RoomescapeException("같은 이름의 테마가 이미 존재합니다.");
        }
    }

    public void validateHavingReservation() {
        if (!reservations.isEmpty()) {
            throw new RoomescapeException("해당 테마를 사용하는 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getDescription() {
        return description.getValue();
    }

    public String getThumbnail() {
        return thumbnail.getUrl();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
