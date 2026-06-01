package roomescape.reservation.application.dao;

import java.util.List;
import roomescape.reservation.application.dto.WaitingDetail;

public interface WaitingDetailDao {
    List<WaitingDetail> findByName(String username);
}
