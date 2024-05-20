package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;

public interface ReservationRepository extends
        ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findAllByMemberId(Long id);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    List<Reservation> findAll(Specification<Reservation> specification);

    default Reservation getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 예약입니다."));
    }
}
