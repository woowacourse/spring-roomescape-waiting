package roomescape.slot;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Slot {
    private final Long id;
    private LocalDate date;
    private Long timeId;
    private Long themeId;

    public static Slot of(Long id, LocalDate date, Long timeId, Long themeId) {
        return new Slot(id, date, timeId, themeId);
    }

    public static Slot create(LocalDate date, long timeId, long themeId) {
        return new Slot(null, date, timeId, themeId);
    }
}
