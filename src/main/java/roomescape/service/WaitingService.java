package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.NotFoundException;
import roomescape.model.Waiting;
import roomescape.model.WaitingSavePolicy;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.LoginMember;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final WaitingSavePolicy waitingSavePolicy;

    public WaitingService(WaitingRepository waitingRepository, WaitingSavePolicy waitingSavePolicy) {
        this.waitingRepository = waitingRepository;
        this.waitingSavePolicy = waitingSavePolicy;
    }

    public List<Waiting> findAllWaitings() {
        return waitingRepository.findAll();
    }

    public Waiting saveWaiting(ReservationDto reservationDto) {
        waitingSavePolicy.validate(reservationDto);

        Waiting waiting = new Waiting(reservationDto);
        return waitingRepository.save(waiting);
    }

    public void deleteWaiting(long id) {
        validateExist(id);
        waitingRepository.deleteById(id);
    }

    public List<WaitingWithRank> findWaitingsWithRankByMember(LoginMember member) {
        List<Waiting> allWaitings = findAllWaitings();
        List<Waiting> waitings = waitingRepository.findByMemberId(member.getId());
        return waitings.stream()
                .map(waiting -> new WaitingWithRank(waiting, allWaitings))
                .toList();
    }

    private void validateExist(long id) {
        boolean isNotExist = !waitingRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 대기입니다.");
        }
    }
}
