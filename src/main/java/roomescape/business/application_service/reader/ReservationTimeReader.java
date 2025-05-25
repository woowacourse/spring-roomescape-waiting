package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeReader {

    private final JdbcClient jdbcClient;

    public List<ReservationTimeDto> getAll() {
        String sql = "SELECT * FROM reservation_time";
        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> new ReservationTimeDto(
                        Id.create(rs.getString("id")),
                        new StartTime(rs.getTime("start_time").toLocalTime())
                ))
                .list();
    }

    public List<ReservableReservationTimeDto> getAllWithAvailableBy(final LocalDate date, final String themeIdValue) {
        String sql = """
                SELECT rt.*, (
                    rt.id NOT IN (
                        SELECT rs.time_id
                        FROM reservation_slot rs
                        WHERE rs.reservation_date = :date
                        AND rs.theme_id = :themeId
                    ) OR rt.id IN (
                        SELECT rs.time_id
                        FROM reservation_slot rs
                        LEFT JOIN reservation r ON r.slot_id = rs.id
                        WHERE rs.reservation_date = :date
                        AND rs.theme_id = :themeId
                        GROUP BY rs.id
                        HAVING COUNT(r.id) = 0
                    )
                ) as available
                FROM reservation_time rt
                ORDER BY rt.start_time
                """;

        return jdbcClient.sql(sql)
                .param("date", date)
                .param("themeId", themeIdValue)
                .query((rs, rowNum) -> new ReservableReservationTimeDto(
                        Id.create(rs.getString("id")),
                        new StartTime(rs.getTime("start_time").toLocalTime()),
                        rs.getBoolean("available")
                ))
                .list();
    }
}
