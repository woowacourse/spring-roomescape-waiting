package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.persistence.JpaBookingQueryRepository;
import roomescape.persistence.dto.MemberBookingProjection;
import roomescape.service.dto.result.MemberBookingResult;

@Service
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
