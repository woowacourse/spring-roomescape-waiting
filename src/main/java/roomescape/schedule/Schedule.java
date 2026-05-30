package roomescape.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Schedule {
    private final Long id;
    private final LocalDate date;
    private final Long timeId;
    private final Long themeId;
}
