package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.ReservationDetail;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
    int countReservationsByTime_Id(Long timeId);
}
