package roomescape.booking.waiting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.waiting.dto.WaitingResponse;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.exception.custom.reason.waiting.WaitingNotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;

    @Transactional(readOnly = true)
    public List<WaitingResponse> readAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::of)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id, LoginMember member) {
        Waiting waiting = getById(id);
        validateAuthorization(member, waiting);
        waitingRepository.delete(waiting);
    }

    @Transactional
    public void deleteByIdForAdmin(final Long id) {
        Waiting waiting = getById(id);
        waitingRepository.delete(waiting);
    }

    @Transactional(readOnly = true)
    public Waiting findFirstWaitingOfSchedule(final Schedule schedule) {
        return waitingRepository.findFirstByScheduleOrderByCreatedAtAsc(schedule);
    }

    @Transactional(readOnly = true)
    public List<Waiting> findAllByEmail(final String email) {
        return waitingRepository.findAllByMember_Email(email);
    }

    @Transactional
    public void delete(final Waiting waiting) {
        waitingRepository.delete(waiting);
    }

    @Transactional(readOnly = true)
    public boolean existsBySchedule(final Schedule schedule) {
        return waitingRepository.existsBySchedule(schedule);
    }

    @Transactional(readOnly = true)
    public Long getRank(final Waiting waiting) {
        return waitingRepository.countByScheduleAndCreatedAtLessThan(waiting.getSchedule(), waiting.getCreatedAt());
    }

    private void validateAuthorization(final LoginMember member, final Waiting waiting) {
        boolean isAuthorized = waiting.getMember().isEmailEquals(member.email());
        if (!isAuthorized) {
            throw new AuthorizationException();
        }
    }

    private Waiting getById(final Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(WaitingNotFoundException::new);
    }
}
