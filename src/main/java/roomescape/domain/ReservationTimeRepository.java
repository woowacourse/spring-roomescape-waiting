package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.service.exception.ResourceNotFoundCustomException;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    default ReservationTime getReservationTimeById(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundCustomException("아이디에 해당하는 예약 시간이 없습니다."));
    }
}
