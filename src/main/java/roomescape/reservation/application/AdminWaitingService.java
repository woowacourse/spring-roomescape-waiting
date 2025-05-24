package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingRepository;
import roomescape.reservation.ui.dto.request.CreateWaitingRequest;
import roomescape.reservation.ui.dto.response.WaitingResponse;
import roomescape.reservation.ui.dto.response.WaitingWithRankResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class AdminWaitingService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingResponse create(final CreateWaitingRequest request) {
        final ReservationTime time = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        return WaitingResponse.from(createWaiting(request.date(), time, theme, member));
    }

    private Waiting createWaiting(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        final ReservationSlot reservationSlot = ReservationSlot.of(date, time, theme);

        if (!reservationRepository.existsByReservationSlot(reservationSlot)) {
            throw new ResourceNotFoundException("예약이 없는 상태에서 예약 대기를 추가할 수 없습니다.");
        }

        if (reservationRepository.existsByReservationSlotAndMember(reservationSlot, member)) {
            throw new AlreadyExistException("해당 예약 슬롯에 본인 예약이 있습니다.");
        }

        if (waitingRepository.existsByReservationSlotAndMember(reservationSlot, member)) {
            throw new AlreadyExistException("신청한 예약 대기가 이미 존재합니다.");
        }

        final Waiting waiting = Waiting.of(
                reservationSlot,
                member,
                LocalDateTime.now()
        );

        return waitingRepository.save(waiting);
    }

    public void deleteAsAdmin(final Long waitingId) {
        if (!waitingRepository.existsById(waitingId)) {
            throw new ResourceNotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }

        waitingRepository.deleteById(waitingId);
    }

    public List<WaitingWithRankResponse> findAllWaitingWithRank() {
        return waitingRepository.findAllWaitingWithRank().stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }

    private ReservationTime getReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));
    }
}
