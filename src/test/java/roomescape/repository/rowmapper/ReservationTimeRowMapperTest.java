package roomescape.repository.rowmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.SQLException;
import java.sql.Types;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationTime;

class ReservationTimeRowMapperTest {

    @DisplayName("RowMapper는 ResultSet을 이용하여 ReservationTime을 매핑한다.")
    @Test
    void return_reservation_by_map_row() throws SQLException {
        SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("id", Types.BIGINT, 10, 0);
        resultSet.addColumn("start_at", Types.VARCHAR, 255, 0);
        resultSet.addRow(1, "20:00");
        resultSet.next();

        ReservationTimeRowMapper rowMapper = new ReservationTimeRowMapper();
        ReservationTime reservationTime = rowMapper.mapRow(resultSet, 1);

        assertAll(
                () -> assertThat(reservationTime.getId()).isEqualTo(1),
                () -> assertThat(reservationTime.getStartAt()).isEqualTo("20:00")
        );
    }
}
