package roomescape.waiting.service;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ForbiddenException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.dto.response.WaitingCreateResponse;
import roomescape.waiting.dto.response.WaitingReadResponse;
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

    public List<WaitingReadResponse> getWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingReadResponse::from)
                .toList();
    }

    public void deleteWaiting(Long waitingId, LoginMember loginMember) {
        validateLoginMemberWithWaiting(waitingId, loginMember);
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void acceptWaiting(Long id) {
        Waiting waiting = getWaitingById(id);

        deleteReserved(waiting);

        waitingRepository.delete(waiting);
        Reservation reservation = new Reservation(
                waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember());
        reservationRepository.save(reservation);
    }

    private void deleteReserved(Waiting waiting) {
        boolean alreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeId(
                waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId());

        if (alreadyReserved) {
            Reservation found = reservationRepository.findByDateAndThemeAndTime(
                            waiting.getDate(), waiting.getTheme(), waiting.getTime())
                    .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));

            reservationRepository.delete(found);
        }
    }

    private Waiting getWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약 대기를 찾을 수 없습니다."));
    }

    private void validateLoginMemberWithWaiting(Long waitingId, LoginMember loginMember) {
        Waiting waiting = getWaitingById(waitingId);
        if (!waiting.getMember().getId().equals(loginMember.id()) && loginMember.role() != RoleType.ADMIN) {
            throw new ForbiddenException("예약 대기를 삭제할 수 있는 권한이 없습니다.");
        }
    }

    private void validateAvailableWaiting(LoginMember loginMember, WaitingCreateRequest request) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId())) {
            throw new BadRequestException("예약이 존재하지 않아 대기를 생성할 수 없습니다.");
        }
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), loginMember.id())) {
            throw new BadRequestException("중복된 예약이 존재합니다.");
        }
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), loginMember.id())) {
            throw new BadRequestException("중복된 예약 대기가 존재합니다.");
        }
    }
}
