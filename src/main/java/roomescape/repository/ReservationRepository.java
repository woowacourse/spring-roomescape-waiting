package roomescape.repository;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAllByMemberEmail(String email);

    default Specification<Reservation> getSearchSpecification(String email, Long themeId, LocalDate dateFrom,
                                                              LocalDate dateTo) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            root.fetch("member");
            root.fetch("theme");
            root.fetch("time");

            if (email != null) {
                predicates.add(builder.equal(root.get("member").get("email"), email));
            }
            if (themeId != null) {
                predicates.add(builder.equal(root.get("theme").get("id"), themeId));
            }
            if (dateFrom != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), dateTo));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
