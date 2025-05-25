package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.global.error.exception.UnauthorizedException;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingCreateResponse;
import roomescape.reservation.dto.response.WaitingReadResponse;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.entity.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public WaitingCreateResponse createWaiting(Long memberId, WaitingCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버 입니다."));
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간 입니다."));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마 입니다."));

        Waiting newWaiting = new Waiting(request.date(), time, theme, member);
        validateReservationForWaiting(newWaiting);
        validateDuplicateWaiting(newWaiting);
        validateDateTime(newWaiting);

        Waiting waiting = waitingRepository.save(newWaiting);
        return WaitingCreateResponse.from(waiting);
    }

    @Transactional(readOnly = true)
    public List<WaitingReadResponse> getAllWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingReadResponse::from)
                .toList();
    }

    @Transactional
    public void deleteWaiting(Long memberId, Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기 입니다."));

        validateOwner(waiting, memberId);

        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void deleteWaitingByAdmin(Long waitingId) {
        waitingRepository.deleteById(waitingId);
    }

    private void validateReservationForWaiting(Waiting waiting) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        ).orElseThrow(() -> new BadRequestException("예약이 존재하지 않는 상태에서는 예약 대기를 신청할 수 없습니다."));

        if (reservation.isOwnedBy(waiting.getMember().getId())) {
            throw new BadRequestException("이미 예약한 사용자는 해당 예약에 대기 신청할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(Waiting waiting) {
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId(),
                waiting.getMember().getId()
        )) {
            throw new BadRequestException("이미 해당 예약에 대한 대기 신청이 존재합니다.");
        }
    }

    private void validateDateTime(Waiting waiting) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime waitingDateTime = waiting.getDateTime();
        if (waitingDateTime.isBefore(now)) {
            throw new BadRequestException("과거 날짜는 예약할 수 없습니다.");
        }
    }

    private void validateOwner(Waiting waiting, Long memberId) {
        if (!waiting.isOwnedBy(memberId)) {
            throw new UnauthorizedException("예약 대기는 본인만 삭제할 수 있습니다.");
        }
    }
}
