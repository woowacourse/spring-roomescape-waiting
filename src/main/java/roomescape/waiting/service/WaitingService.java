package roomescape.waiting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.InvalidTimeException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationExceptionMessage;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;

@Service
@AllArgsConstructor
public class WaitingService {
    public static final int FIRST_WAITING = 1;
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

    public void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll().stream().toList();
        return WaitingResponse.from(waitings);
    }

    public void approve(Long id) {
        //TODO: 커스텀 예외 구현하기  (2025-05-20, 화, 2:9)
        WaitingWithRank waitingWithRank = waitingRepository.findWithRankById(id)
                .orElseThrow(() -> new InvalidIdException("존재하지 않는 예약 대기"));

        if (waitingWithRank.getRank() != FIRST_WAITING) {
            throw new InvalidIdException("우선순위가 낮은 예약 대기");
        }

        Waiting waiting = waitingWithRank.getWaiting();

        if (reservationRepository.existsBySpec(waiting.getSpec())) {
            throw new InvalidIdException("예약이 존재하는 슬롯");
        }
        Reservation reservation = new Reservation(waiting.getMember(), waiting.getSpec());
        reservationRepository.save(reservation);
        deleteById(id);
    }
}
