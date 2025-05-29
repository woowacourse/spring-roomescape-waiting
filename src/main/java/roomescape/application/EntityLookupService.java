package roomescape.application;

import org.springframework.stereotype.Service;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.exception.NotFoundException;

@Service
public class EntityLookupService {

    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public EntityLookupService(
            final UserRepository userRepository,
            final TimeSlotRepository timeSlotRepository,
            final ThemeRepository themeRepository
    ) {
        this.userRepository = userRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    public User getUserById(final long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

    public TimeSlot getTimeSlotById(final long timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타임 슬롯입니다."));
    }

    public Theme getThemeById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }
} 
