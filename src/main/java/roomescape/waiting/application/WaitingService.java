package roomescape.waiting.application;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.exception.AccessForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationInPastException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.exception.TimeNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.waiting.application.dto.WaitingRequest;
import roomescape.waiting.application.dto.WaitingResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.exception.NotFirstWaitingException;
import roomescape.waiting.exception.SlotNotReservedException;
import roomescape.waiting.exception.WaitingNotFoundException;

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
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        ReservationDate date = new ReservationDate(request.date());
        ReservationTime time = timeRepository.findById(request.timeId()).orElseThrow(TimeNotFoundException::new);
        validateInPast(date, time);

        Theme theme = themeRepository.findById(request.themeId()).orElseThrow(ThemeNotFoundException::new);

        ReservationSpec spec = new ReservationSpec(date, time, theme);
        validateReservationExists(spec);

        Waiting waiting = new Waiting(member, spec);
        return WaitingResponse.from(waitingRepository.save(waiting));
    }

    private void validateReservationExists(ReservationSpec spec) {
        if (!reservationRepository.existsBySpec(spec)) {
            throw new SlotNotReservedException();
        }
    }

    private void validateInPast(ReservationDate date, ReservationTime time) {
        if (date.isInPast() || date.isToday() && time.isBeforeNow()) {
            throw new ReservationInPastException();
        }
    }

    public void deleteByUser(Long id, Long memberId) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow(WaitingNotFoundException::new);
        validateIsOwner(memberId, waiting);
        deleteById(id);
    }

    private void validateIsOwner(Long memberId, Waiting waiting) {
        if (!waiting.isOwnedBy(memberId)) {
            throw new AccessForbiddenException();
        }
    }

    public void deleteByAdmin(Long id) {
        deleteById(id);
    }

    private void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll().stream().toList();
        return WaitingResponse.from(waitings);
    }

    public void approve(Long id) {
        WaitingWithRank waitingWithRank = waitingRepository.findWithRankById(id)
                .orElseThrow(WaitingNotFoundException::new);
        validateIsFirst(waitingWithRank);

        Waiting waiting = waitingWithRank.getWaiting();
        validateReservationExists(waiting);

        Reservation reservation = new Reservation(waiting.getMember(), waiting.getSpec());
        deleteById(id);
        reservationRepository.save(reservation);
    }

    private void validateIsFirst(WaitingWithRank waitingWithRank) {
        if (waitingWithRank.getRank() != FIRST_WAITING) {
            throw new NotFirstWaitingException();
        }
    }

    private void validateReservationExists(Waiting waiting) {
        if (reservationRepository.existsBySpec(waiting.getSpec())) {
            throw new ReservationAlreadyExistsException();
        }
    }
}
