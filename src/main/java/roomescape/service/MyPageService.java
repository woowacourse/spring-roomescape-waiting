package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.persistence.JpaBookingQueryRepository;
import roomescape.persistence.dto.MemberBookingProjection;
import roomescape.service.dto.result.MemberBookingResult;

@Service
@Transactional(readOnly = true)
public class MyPageService {

    private final JpaBookingQueryRepository jpaBookingQueryRepository;

    public MyPageService(JpaBookingQueryRepository jpaBookingQueryRepository) {
        this.jpaBookingQueryRepository = jpaBookingQueryRepository;
    }

    public List<MemberBookingResult> getMyBookings(Long memberId) {
        List<MemberBookingProjection> bookings = jpaBookingQueryRepository.findAllBookingsByMemberId(memberId);
        return MemberBookingResult.from(bookings);
    }
}
