package roomescape.schedule.service;

import org.springframework.stereotype.Service;
import roomescape.schedule.domain.Schedule;
import roomescape.schedule.respository.ScheduleRepository;

import java.util.Optional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Schedule save(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Optional<Schedule> findById(Long id) {
        return scheduleRepository.findById(id);
    }
}
