package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Waitlist;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class WaitlistService {
    private final WaitlistRepository waitlistRepository;

    public WaitlistService(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @Transactional
    public void cancelMyWaitlist(Long id, String name) {
        Waitlist waitlist = getWaitlist(id);
        waitlist.verifyCancelableBy(name);
        waitlistRepository.deleteById(id);
    }

    public Waitlist getWaitlist(Long id) {
        return waitlistRepository.getById(id, "존재하지 않는 예약 대기입니다.");
    }
}
