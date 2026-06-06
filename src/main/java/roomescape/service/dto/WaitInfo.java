package roomescape.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Wait;
import roomescape.repository.dto.WaitDetailDto;

public record WaitInfo(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        ReservationStatus status,
        Long order,
        LocalDateTime createdAt
) {
    public static WaitInfo from(WaitDetailDto waitDetailDto) {
        return new WaitInfo(
                waitDetailDto.id(),
                waitDetailDto.name(),
                waitDetailDto.reservationDate(),
                ReservationTimeInfo.from(waitDetailDto.reservationTime()),
                ThemeInfo.from(waitDetailDto.theme()),
                ReservationStatus.WAITING,
                waitDetailDto.order(),
                waitDetailDto.createdAt()
        );
    }

    public static WaitInfo of(Wait wait, Long order) {
        return new WaitInfo(
                wait.getId(),
                wait.getName(),
                wait.getReservationDate(),
                ReservationTimeInfo.from(wait.getTime()),
                ThemeInfo.from(wait.getTheme()),
                ReservationStatus.WAITING,
                order,
                wait.getCreatedAt()
        );
    }
}
