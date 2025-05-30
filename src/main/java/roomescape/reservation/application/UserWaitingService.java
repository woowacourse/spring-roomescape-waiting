package roomescape.reservation.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.application.dto.request.CreateWaitingServiceRequest;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.exception.ReservationException.InvalidReservationTimeException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class UserWaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public WaitingServiceResponse create(CreateWaitingServiceRequest request) {
        validateNoDuplication(request.date(), request.timeId(), request.themeId(),
            request.memberId());

        ReservationTheme theme = reservationThemeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Member member = memberRepository.getById(request.memberId());

        Waiting waiting = Waiting.builder()
            .date(request.date())
            .theme(theme)
            .time(time)
            .member(member)
            .build();
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingServiceResponse.from(savedWaiting);
    }

    public void deleteMyWaitingById(Long id, Long memberId) {
        Waiting waiting = waitingRepository.findByIdAndMemberId(id, memberId)
            .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 예약 대기가 존재하지 않습니다."));
        waitingRepository.delete(waiting);
    }

    public void validateNoDuplication(LocalDate date, Long timeId, Long themeId, Long memberId) {
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId,
            memberId) ||
            reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId,
                memberId)) {
            throw new InvalidReservationTimeException("이미 대기중인 예약입니다.");
        }
    }
}
