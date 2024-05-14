package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("SELECT t FROM Theme t")
    List<Theme> findTopThemesDescendingByDescription(String startDate, String endDate, int count);
}
