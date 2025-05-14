package roomescape.service.reservation;

import java.util.List;
import roomescape.domain.Member;
import roomescape.dto.admin.AdminReservationRequest;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.search.SearchConditionsRequest;

public interface ReservationService {

    ReservationResponse create(ReservationRequest request, Member member);

    List<ReservationResponse> getAll();

    void deleteById(Long id);

    ReservationResponse createByAdmin(AdminReservationRequest adminReservationRequest);

    List<ReservationResponse> getReservationsByConditions(SearchConditionsRequest searchConditionsRequest);

    List<MemberReservationResponse> getReservationByMember(Member member);
}
