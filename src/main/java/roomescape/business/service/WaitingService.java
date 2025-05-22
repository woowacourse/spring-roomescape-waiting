package roomescape.business.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Member;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.Waiting;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.WaitingRequest;
import roomescape.presentation.dto.WaitingResponse;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingService(final WaitingRepository waitingRepository,
                          final ThemeRepository themeRepository,
                          final MemberRepository memberRepository, final ReservationRepository reservationRepository,
                          final ReservationTimeRepository reservationTimeRepository) {
        this.waitingRepository = waitingRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional
    public WaitingResponse insert(final LoginMember loginMember, final WaitingRequest waitingRequest) {
        final Theme theme = getThemeById(waitingRequest.themeId());
        final ReservationTime reservationTime = getReservationTimeById(waitingRequest.timeId());
        validateIfReservationNotExists(waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId());

        final Member member = getMemberById(loginMember.id());

        final Waiting waiting = new Waiting(member, theme, reservationTime, waitingRequest.date());
        Waiting savedWaiting = waitingRepository.save(waiting);
        // ToDo: 최적화 필요
        return WaitingResponse.from(savedWaiting);
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다. 테마 id: %d".formatted(themeId)));
    }

    private ReservationTime getReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("해당하는 방탈출 예약 시간을 찾을 수 없습니다. 방탈출 id: %d".formatted(timeId)));
    }

    private void validateIfReservationNotExists(final LocalDate date, final Long timeId, final Long themeId) {
        boolean isReservationExisted = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId,
                themeId);
        if (!isReservationExisted) {
            throw new NotFoundException("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("해당하는 사용자를 찾을 수 없습니다. 사용자 id: %d".formatted(memberId)));
    }

    public void deleteById(final Long id) {
        waitingRepository.deleteById(id);
    }
}
