package roomescape.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.ReservationWaiting;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.dto.ReservationWaitingRequest;
import roomescape.service.dto.ReservationWaitingResponse;

@Service
@RequiredArgsConstructor
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationThemeService reservationThemeService;

    public ReservationWaitingResponse addReservationWaiting(final ReservationWaitingRequest request, final long memberId) {
        final Member member = memberService.getMemberById(memberId);
        validateDuplicate(request, memberId);
        final ReservationWaiting reservation = new ReservationWaiting(member, request.date(),
                reservationTimeService.getById(request.timeId()), reservationThemeService.getById(request.themeId()));
        return ReservationWaitingResponse.from(reservationWaitingRepository.save(reservation));
    }

    public void removeReservationWaiting(final long id) {
        validateReservationWaiting(id);
        reservationWaitingRepository.deleteById(id);
    }

    private void validateReservationWaiting(final long id) {
        if (!reservationWaitingRepository.existsById(id)) {
            throw new NoSuchElementException("[ERROR] 존재하지 않는 예약대기 입니다.");
        }
    }

    private void validateDuplicate(final ReservationWaitingRequest request, final long memberId) {
        if (reservationWaitingRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
                memberId,
                request.themeId(),
                request.timeId(),
                request.date()
        )) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약 대기 입니다.");
        }
    }
}
