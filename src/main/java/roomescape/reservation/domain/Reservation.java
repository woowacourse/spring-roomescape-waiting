package roomescape.reservation.domain;

import java.time.LocalDate;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode(of = {"date", "themeId", "timeId"})
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Long themeId;
    private final Long timeId;

    public static Reservation of(Long id, String name, LocalDate date, Long themeId, Long timeId) {
        return new Reservation(id, name, date, themeId, timeId);
    }

    public Reservation withId(Long generatedId) {
        return Reservation.builder()
                .id(generatedId)
                .name(this.name)
                .date(this.date)
                .themeId(this.themeId)
                .timeId(this.timeId)
                .build();
    }

    public Reservation update(LocalDate date, Long timeId) {
        return Reservation.builder()
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
