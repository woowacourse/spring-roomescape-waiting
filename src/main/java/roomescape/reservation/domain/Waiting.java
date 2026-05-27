package roomescape.reservation.domain;

import java.time.LocalDate;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(of = {"name", "date", "themeId", "timeId"})
public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Long themeId;
    private final Long timeId;
    
    public static Waiting of(Long id, String name, LocalDate date, Long themeId, Long timeId) {
        return new Waiting(id, name, date, themeId, timeId);
    }

    public Waiting withId(Long generatedId) {
        return Waiting.builder()
                .id(generatedId)
                .name(this.name)
                .date(this.date)
                .themeId(this.themeId)
                .timeId(this.timeId)
                .build();
    }

    public Waiting update(LocalDate date, Long timeId) {
        return Waiting.builder()
                .id(this.id)
                .name(this.name)
                .date(date)
                .themeId(this.themeId)
                .timeId(timeId)
                .build();
    }

    public boolean isOwner(String name) {
        return this.name.equals(name);
    }
}
