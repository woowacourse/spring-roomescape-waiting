package roomescape.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.InvalidTimeException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationExceptionMessage;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;

@Service
@AllArgsConstructor
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public WaitingResponse add(Long memberId, WaitingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_RESERVATION_ID.getMessage()));
        LocalDate date = request.date();
        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_THEME_ID.getMessage()));

        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException(ReservationExceptionMessage.TIME_BEFORE_NOW.getMessage());
        }

        ReservationSpec spec = new ReservationSpec(date, time, theme);
        if (!reservationRepository.existsBySpec(spec)) {
            throw new IllegalStateException("바로 예약이 가능한 슬롯입니다.");
        }

        Waiting waiting = new Waiting(member, spec);
        return WaitingResponse.from(waitingRepository.save(waiting));
    }
}
