package roomescape.domain.repository;

import java.time.LocalDate;

public interface ReservationSlotRepositoryCustom {
    Long save(LocalDate date, long timeId, long themeId);
}
