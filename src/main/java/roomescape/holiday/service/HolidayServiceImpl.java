package roomescape.holiday.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.holiday.domain.Holiday;
import roomescape.holiday.exception.HolidayNotFoundException;
import roomescape.holiday.repository.HolidayRepository;
import roomescape.holiday.service.dto.HolidaySaveServiceRequest;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
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
    @Transactional
    public Holiday create(HolidaySaveServiceRequest holiday) {
        return holidayRepository.save(new Holiday(holiday.date()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new HolidayNotFoundException(id);
        }
        holidayRepository.deleteById(id);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByDate(date);
    }
}
