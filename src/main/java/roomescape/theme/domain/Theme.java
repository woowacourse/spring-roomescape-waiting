package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Theme {

    private static final Duration THEME_USAGE_DURATION = Duration.ofHours(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Duration getDuration() {
        return THEME_USAGE_DURATION;
    }
}
