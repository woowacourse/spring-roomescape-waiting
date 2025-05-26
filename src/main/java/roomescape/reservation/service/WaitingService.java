package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationChecker reservationChecker;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository, ReservationChecker reservationChecker) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationChecker = reservationChecker;
    }

    public List<WaitingResponse> findAll() {
        List<Waiting> allWaitings = waitingRepository.findAll();
        return allWaitings.stream()
                .map(waiting -> WaitingResponse.from(waiting, waiting.getTime(), waiting.getTheme()))
                .toList();
    }

    @Transactional
    public WaitingResponse createWaiting(WaitingRequest dto, Member member) {
        Waiting waiting = reservationChecker.createWaitingWithoutId(dto, member);

        validate(waiting);

        Waiting newWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(newWaiting, newWaiting.getTime(), newWaiting.getTheme());
    }

    private void validate(Waiting waiting) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId(), waiting.getMember().getId())) {
            throw new DuplicateContentException("[ERROR] 이미 예약한 건에는 예약대기를 걸 수 없습니다.");
        }

        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId(), waiting.getMember().getId())) {
            throw new DuplicateContentException("[ERROR] 해당 날짜와 테마로 이미 예약대기 내역이 존재합니다.");
        }
    }

    @Transactional
    public void deleteWaiting(Long id, Long memberId) {
        if (waitingRepository.findByIdAndMemberId(id, memberId).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약대기만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        waitingRepository.deleteById(id);
    }

    @Transactional
    public void deleteWaitingById(Long id) {
        if (waitingRepository.findById(id).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약대기만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        waitingRepository.deleteById(id);
    }
}
