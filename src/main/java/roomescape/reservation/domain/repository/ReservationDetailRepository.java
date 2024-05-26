package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, Long>, JpaSpecificationExecutor<ReservationDetail> {

    default ReservationDetail getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_DETAIL_NOT_FOUND,
                        String.format("예약 정보(ReservationDetail)가 존재하지 않습니다. [reservationDetailId: %d]", id)));
    }

    List<ReservationDetail> findByReservationTime(ReservationTime reservationTime);

    Optional<ReservationDetail> findByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);

    // TODO: API 부활시키기
//    default List<Reservation> searchWith(Theme theme, Member member, LocalDate dateFrom, LocalDate dateTo) {
//        return this.findAll((root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            if (theme != null) {
//                predicates.add(cb.equal(root.get("theme"), theme));
//            }
//            if (member != null) {
//                predicates.add(cb.equal(root.get("member"), member));
//            }
//            if (dateFrom != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
//            }
//            if (dateTo != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
//            }
//            return cb.and(predicates.toArray(new Predicate[0]));
//        });
//    }

    List<ReservationDetail> findByDateAndTheme(LocalDate date, Theme theme);
}
