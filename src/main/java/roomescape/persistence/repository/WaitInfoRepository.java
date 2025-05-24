package roomescape.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.domain.WaitInfo;

public interface WaitInfoRepository extends JpaRepository<WaitInfo, Long> {

    List<WaitInfo> findByMemberId(Long memberId);

    boolean existsByReservationId(Long reservationId);

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    boolean existsByIdAndMemberId(Long waitInfoId, Long memberId);

    Long countByIdLessThanEqualAndReservationId(Long id, Long reservationId);

    List<WaitInfo> findByRankNot(Long rank);
}
