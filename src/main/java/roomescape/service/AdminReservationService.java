package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.dto.AdminReservationResponse;
import roomescape.repository.ScheduleDao;

@Service
public class AdminReservationService {

    private final ScheduleDao scheduleDao;

    public AdminReservationService(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return scheduleDao.findAll().stream()
                .map(s -> AdminReservationResponse.from(s, s.getTheme()))
                .toList();
    }
}
