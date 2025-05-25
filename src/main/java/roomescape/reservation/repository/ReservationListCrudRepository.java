package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.lang.NonNull;
import roomescape.reservation.domain.Reservation;

public interface ReservationListCrudRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @NonNull
    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.details.time
            JOIN FETCH r.details.theme
            """)
    List<Reservation> findAll();

    List<Reservation> findByDetails_DateAndDetails_Theme_Id(LocalDate date, Long themeId);

    Optional<Reservation> findByDetails_Time_Id(Long id);

    Optional<Reservation> findByDetails_Theme_Id(Long id);

    boolean existsByDetails_DateAndDetails_Time_IdAndDetails_Theme_Id(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);
}
