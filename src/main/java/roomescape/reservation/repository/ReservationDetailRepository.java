package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import roomescape.reservation.domain.ReservationDetail;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long> {
}
