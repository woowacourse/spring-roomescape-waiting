package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingCreationContent;
import roomescape.dto.response.WaitingResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository reservationTimeRepository,
            MemberRepository memberRepository, ReservationRepository reservationRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<WaitingResponse> findAllWaiting() {
        List<Waiting> allWaiting = waitingRepository.findAll();
        return allWaiting.stream().map(WaitingResponse::new)
                .toList();
    }

    public WaitingResponse addWaiting(WaitingCreationContent content) {
        Theme theme = getThemeById(content.themeId());
        ReservationTime time = getTimeById(content.timeId());
        Member member = getMemberById(content.memberId());
        Waiting waiting = Waiting.createWithoutId(content.date(), theme, time, member);

        validateEmptyReservation(waiting);
        validatePastWaitingCreation(waiting);
        validateDuplicatedWaiting(waiting);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    public void deleteWaitingById(long id) {
        Waiting waiting = getWaitingById(id);
        waitingRepository.delete(waiting);
    }

    public void deleteMineWaitingById(long memberId, long waitingId) {
        Waiting waiting = getWaitingById(waitingId);
        validateMineWaiting(memberId, waiting);
        waitingRepository.delete(waiting);
    }

    private Waiting getWaitingById(long waitingId) {
        return waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 대기가 존재하지 않습니다."));
    }

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 테마가 존재하지 않습니다."));
    }

    private ReservationTime getTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 예약시간이 존재하지 않습니다."));
    }

    private Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("ID에 해당하는 회원이 존재하지 않습니다."));
    }

    private void validatePastWaitingCreation(Waiting waiting) {
        boolean isPast = waiting.isPastWaiting();
        if (isPast) {
            throw new BadRequestException("과거 날짜와 시간으로 예약 대기를 생성할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(Waiting waiting) {
        boolean isDuplicated = waitingRepository.existsDuplicated(
                waiting.getTheme().getId(), waiting.getDate(), waiting.getTime().getId(), waiting.getMember().getId());
        if (isDuplicated) {
            throw new BadRequestException("중복된 예약 대기는 허용하지 않습니다.");
        }
    }

    private void validateEmptyReservation(Waiting waiting) {
        boolean isExisted = reservationRepository.existsByThemeAndDateAndReservationTime(
                waiting.getTheme(), waiting.getDate(), waiting.getTime());
        if (!isExisted) {
            throw new BadRequestException("예약이 존재하지 않는 예약 대기는 허용하지 않습니다.");
        }
    }

    private void validateMineWaiting(long memberId, Waiting waiting) {
        if (!waiting.isEqualMember(memberId)) {
            throw new ForbiddenException("다른 회원의 예약대기입니다.");
        }
    }
}
