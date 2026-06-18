package roomescape.holiday.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.holiday.domain.Holiday;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByDate(LocalDate date);
}
