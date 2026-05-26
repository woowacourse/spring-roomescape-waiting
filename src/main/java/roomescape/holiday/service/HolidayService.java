package roomescape.holiday.service;

import java.time.LocalDate;
import java.util.List;

import roomescape.holiday.domain.Holiday;
import roomescape.holiday.service.dto.HolidaySaveServiceDto;

public interface HolidayService {
    List<Holiday> getAll();

    Holiday create(HolidaySaveServiceDto holiday);

    void delete(Long id);

    boolean isHoliday(LocalDate date);
}
