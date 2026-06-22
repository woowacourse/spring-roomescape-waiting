package roomescape.domain.theme;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomEscapeException;

import java.time.LocalDate;
import java.util.List;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("SELECT t FROM Theme t " +
            "JOIN Slot s ON s.theme = t " +
            "JOIN Reservation r ON r.slot = s " +
            "WHERE s.date.date > :fromDate AND s.date.date <= :date " +
            "GROUP BY t " +
            "ORDER BY COUNT(r) DESC")
    List<Theme> findFamous(@Param("fromDate") LocalDate fromDate, @Param("date") LocalDate date, Pageable pageable);

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 테마를 찾을 수 없습니다. : " + id));
    }
}
