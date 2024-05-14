package roomescape.reservation.domain.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByReservationTime(ReservationTime reservationTime);

    List<Reservation> findByReservationTimeAndDateAndTheme(ReservationTime reservationTime, LocalDate date, Theme theme);

    default List<Reservation> findByThemeIdAndMemberIdBetweenDate(Theme theme, Member member, LocalDate dateFrom, LocalDate dateTo) {
        return this.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (theme != null) {
                predicates.add(cb.equal(root.get("theme"), theme));
            }
            if (member != null) {
                predicates.add(cb.equal(root.get("member"), member));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    List<Reservation> findByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);
}
