package roomescape.holiday.repository;

import java.time.LocalDate;
import roomescape.holiday.domain.Holiday;

import java.util.List;

public interface HolidayRepository {
    Holiday save(Holiday holiday);

    List<Holiday> findAll();

    boolean existsByDate(LocalDate date);

    boolean deleteById(Long id);
}
