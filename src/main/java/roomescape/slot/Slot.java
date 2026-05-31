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
}
