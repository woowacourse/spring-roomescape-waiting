package roomescape.reservation.model.vo;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record Schedule(
        LocalDate date,
        Long timeId,
        Long themeId
) {

}
