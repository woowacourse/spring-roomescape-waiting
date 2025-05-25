package roomescape.business.dto;

import org.springframework.jdbc.core.RowMapper;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

public record ReservationTimeDto(
        Id id,
        StartTime startTime
) {
    public static ReservationTimeDto fromEntity(final ReservationTime time) {
        return new ReservationTimeDto(
                time.getId(),
                time.getStartTime()
        );
    }

    public static RowMapper<ReservationTimeDto> ROW_MAPPER = (rs, rowNum) -> new ReservationTimeDto(
            Id.create(rs.getString("id")),
            new StartTime(rs.getTime("start_time").toLocalTime())
    );
}
