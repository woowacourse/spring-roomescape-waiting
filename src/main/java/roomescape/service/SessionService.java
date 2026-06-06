package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.SessionRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@Service
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    public SessionService(SessionRepository sessionRepository, TimeSlotRepository timeSlotRepository,
                          ThemeRepository themeRepository) {
        this.sessionRepository = sessionRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Session resolveSession(Session targetSession) {
        return sessionRepository.findByDateAndTimeIdAndThemeId(
                targetSession.getDate(), targetSession.getTimeSlot().getId(), targetSession.getTheme().getId()
        ).orElseGet(() -> sessionRepository.save(targetSession));
    }

    public Session resolveNewSession(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = findTimeSlotOrNull(timeId);
        Theme theme = findThemeOrNull(themeId);
        return resolveSession(Session.transientOf(date, timeSlot, theme));
    }

    public Session findSessionOrNull(LocalDate date, Long timeId, Long themeId) {
        return sessionRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).orElse(null);
    }

    @Transactional
    public void deleteSession(long id) {
        sessionRepository.deleteById(id);
    }

    public TimeSlot findTimeSlotOrNull(Long timeId) {
        if (timeId == null) {
            return null;
        }
        return timeSlotRepository.findById(timeId).orElseThrow(() -> new TimeSlotNotFoundException(timeId));
    }

    public Theme findThemeOrNull(Long themeId) {
        if (themeId == null) {
            return null;
        }
        return themeRepository.findById(themeId).orElseThrow(() -> new ThemeNotFoundException(themeId));
    }
}
