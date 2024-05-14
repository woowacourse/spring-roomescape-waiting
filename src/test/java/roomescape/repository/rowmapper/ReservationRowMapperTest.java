package roomescape.repository.rowmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.SQLException;
import java.sql.Types;
import org.h2.tools.SimpleResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;

class ReservationRowMapperTest {

    @DisplayName("RowMapper는 ResultSet을 이용하여 Reservation을 매핑한다.")
    @Test
    void return_reservation_by_map_row() throws SQLException {
        SimpleResultSet resultSet = new SimpleResultSet();
        resultSet.addColumn("reservation_id", Types.BIGINT, 10, 0);
        resultSet.addColumn("member_id", Types.BIGINT, 10, 0);
        resultSet.addColumn("member_email", Types.VARCHAR, 255, 0);
        resultSet.addColumn("member_password", Types.VARCHAR, 255, 0);
        resultSet.addColumn("member_name", Types.VARCHAR, 255, 0);
        resultSet.addColumn("member_role", Types.VARCHAR, 255, 0);
        resultSet.addColumn("theme_id", Types.BIGINT, 10, 0);
        resultSet.addColumn("theme_name", Types.VARCHAR, 255, 0);
        resultSet.addColumn("theme_description", Types.VARCHAR, 255, 0);
        resultSet.addColumn("theme_thumbnail", Types.VARCHAR, 255, 0);
        resultSet.addColumn("reservation_date", Types.VARCHAR, 255, 0);
        resultSet.addColumn("time_id", Types.BIGINT, 10, 0);
        resultSet.addColumn("time_value", Types.VARCHAR, 255, 0);
        resultSet.addRow(1, 1, "t1@t1.com", "123", "재즈", "MEMBER", 1, "재즈의 프로그래밍 모험", "설명", "hi.jpg", "2024-04-24", 1,
                "20:00");
        resultSet.next();

        ReservationRowMapper rowMapper = new ReservationRowMapper();
        Reservation reservation = rowMapper.mapRow(resultSet, 1);

        assertAll(
                () -> assertThat(reservation.getId()).isEqualTo(1),
                () -> assertThat(reservation.getMemberId()).isEqualTo(1),
                () -> assertThat(reservation.getMemberName()).isEqualTo("재즈"),
                () -> assertThat(reservation.getThemeId()).isEqualTo(1),
                () -> assertThat(reservation.getTheme().getName()).isEqualTo("재즈의 프로그래밍 모험"),
                () -> assertThat(reservation.getTheme().getDescription()).isEqualTo("설명"),
                () -> assertThat(reservation.getTheme().getThumbnail()).isEqualTo("hi.jpg"),
                () -> assertThat(reservation.getDate()).isEqualTo("2024-04-24"),
                () -> assertThat(reservation.getTimeId()).isEqualTo(1),
                () -> assertThat(reservation.getTimeStartAt()).isEqualTo("20:00")
        );
    }
}
