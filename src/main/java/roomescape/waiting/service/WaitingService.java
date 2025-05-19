package roomescape.waiting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingCreateResponse createWaiting(LoginMember loginMember, WaitingCreateRequest request) {
        validateAvailableWaiting(loginMember, request);

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));

        Waiting waiting = new Waiting(request.date(), theme, time, member);

        Waiting saved = waitingRepository.save(waiting);
        Long rank = waitingRepository.countByDateAndThemeAndMember(request.date(), theme, member);

        return WaitingCreateResponse.from(saved, rank);
    }

    public void deleteWaiting(Long waitingId) {
        waitingRepository.deleteById(waitingId);
    }

    private void validateAvailableWaiting(LoginMember loginMember, WaitingCreateRequest request) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())) {
            throw new BadRequestException("예약이 존재하지 않아 대기를 생성할 수 없습니다.");
        }
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(), request.themeId(), loginMember.id())) {
            throw new BadRequestException("중복된 예약이 존재합니다.");
        }
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(), request.themeId(), loginMember.id())) {
            throw new BadRequestException("중복된 예약 대기가 존재합니다.");
        }
    }
}
