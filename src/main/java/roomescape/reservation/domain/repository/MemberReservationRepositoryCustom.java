package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.domain.MemberReservation;

public interface MemberReservationRepositoryCustom {
    List<MemberReservation> findBy(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);
}
