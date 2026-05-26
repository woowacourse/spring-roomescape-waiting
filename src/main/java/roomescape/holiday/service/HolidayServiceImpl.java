package roomescape.holiday.service;

import org.springframework.stereotype.Service;

import roomescape.holiday.domain.Holiday;
import roomescape.holiday.exception.HolidayNotFoundException;
import roomescape.holiday.repository.HolidayRepository;
import roomescape.holiday.service.dto.HolidaySaveServiceDto;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayServiceImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    public List<Holiday> getAll() {
        return holidayRepository.findAll();
    }

    @Override
    public Holiday create(HolidaySaveServiceDto holiday) {
        return holidayRepository.save(new Holiday(holiday.date()));
    }

    @Override
    public void delete(Long id) {
        boolean deleted = holidayRepository.deleteById(id);
        if (!deleted) {
            throw new HolidayNotFoundException(id);
        }
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
}
