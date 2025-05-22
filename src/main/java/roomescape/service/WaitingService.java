package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingCreationContent;
import roomescape.dto.response.WaitingResponse;
import roomescape.exception.local.DuplicatedWaitingException;
import roomescape.exception.local.NotCreateWaitingInEmptyReservationException;
import roomescape.exception.local.NotFoundMemberException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.PastWaitingCreationException;
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

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }

    private ReservationTime getTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    private Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(NotFoundMemberException::new);
    }

    private void validatePastWaitingCreation(Waiting waiting) {
        boolean isPast = waiting.isPastWaiting();
        if (isPast) {
            throw new PastWaitingCreationException();
        }
    }

    private void validateDuplicatedWaiting(Waiting waiting) {
        boolean isDuplicated = waitingRepository.existsDuplicated(
                waiting.getTheme().getId(), waiting.getTime().getId(), waiting.getMember().getId());
        if (isDuplicated) {
            throw new DuplicatedWaitingException();
        }
    }

    private void validateEmptyReservation(Waiting waiting) {
        boolean isExisted = reservationRepository.existsByThemeAndDateAndReservationTime(
                waiting.getTheme(), waiting.getDate(), waiting.getTime());
        if (!isExisted) {
            throw new NotCreateWaitingInEmptyReservationException();
        }
    }
}
