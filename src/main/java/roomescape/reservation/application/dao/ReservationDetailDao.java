package roomescape.reservation.application.dao;

import java.util.List;
import roomescape.reservation.application.dto.ReservationDetail;

public interface ReservationDetailDao {
    List<ReservationDetail> findAllByPage(int limit, long offset);

    long countAll();

    List<ReservationDetail> findByName(String username);
}
