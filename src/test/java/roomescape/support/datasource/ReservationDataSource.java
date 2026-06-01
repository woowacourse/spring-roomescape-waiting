package roomescape.support.datasource;

import org.springframework.stereotype.Component;

@Component
public class ReservationDataSource extends BaseDataSource {

    public boolean hasReservationById(Long id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }
}
