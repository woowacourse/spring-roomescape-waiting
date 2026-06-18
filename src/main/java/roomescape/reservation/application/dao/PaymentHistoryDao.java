package roomescape.reservation.application.dao;

import java.util.List;
import roomescape.reservation.application.dto.PaymentHistoryDetail;

public interface PaymentHistoryDao {

    List<PaymentHistoryDetail> findByName(String username);
}
