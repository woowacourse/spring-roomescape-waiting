package roomescape.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Schedule {
    private final Long id;
    private LocalDate date;
    private Long timeId;
    private Long themeId;
}
