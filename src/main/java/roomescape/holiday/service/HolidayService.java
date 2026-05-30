package roomescape.holiday.service;

import roomescape.holiday.domain.Holiday;
import roomescape.holiday.service.dto.HolidaySaveServiceRequest;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {
    List<Holiday> getAll();

    Holiday create(HolidaySaveServiceRequest holiday);

    void delete(Long id);

    boolean isHoliday(LocalDate date);
}
