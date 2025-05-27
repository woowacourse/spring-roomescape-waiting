package roomescape.waiting.application;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.exception.AccessForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
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
import roomescape.waiting.domain.Waitings;
import roomescape.waiting.exception.DuplicatedWaitingException;
import roomescape.waiting.exception.SlotNotReservedException;
import roomescape.waiting.exception.TooManyWaitingException;
import roomescape.waiting.exception.WaitingNotFoundException;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class WaitingService {
    private static final int MAX_WAITING_COUNT = 100;

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public WaitingResponse create(Long memberId, WaitingRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        ReservationDate date = new ReservationDate(request.date());
        ReservationTime time = timeRepository.findById(request.timeId()).orElseThrow(TimeNotFoundException::new);
        validateInPast(date, time);

        Theme theme = themeRepository.findById(request.themeId()).orElseThrow(ThemeNotFoundException::new);

        ReservationSpec spec = new ReservationSpec(date, time, theme);
        validateReservedByOthers(member, spec);

        validateMaxWaitingCount(spec);

        Waiting waiting = new Waiting(member, spec);
        return WaitingResponse.from(waitingRepository.save(waiting));
    }

    private void validateMaxWaitingCount(ReservationSpec spec) {
        if (waitingRepository.findBySpec(spec).size() >= MAX_WAITING_COUNT) {
            throw new TooManyWaitingException();
        }
    }

    private void validateReservedByOthers(Member member, ReservationSpec spec) {
        Optional<Reservation> reservation = reservationRepository.findBySpec(spec);

        validateReservationExists(reservation);
        validateDuplicated(member, reservation.get());
    }

    private void validateReservationExists(Optional<Reservation> reservation) {
        if (reservation.isEmpty()) {
            throw new SlotNotReservedException();
        }
    }

    private void validateDuplicated(Member member, Reservation reservation) {
        Waitings waitings = new Waitings(waitingRepository.findBySpec(reservation.getSpec()));
        if (reservation.getMember().equals(member) || waitings.containsMember(member)) {
            throw new DuplicatedWaitingException();
        }
    }

    private void validateInPast(ReservationDate date, ReservationTime time) {
        if (date.isInPast() || date.isToday() && time.isBeforeNow()) {
            throw new ReservationInPastException();
        }
    }

    @Transactional
    public void deleteByUser(Long id, Long memberId) {
        Waiting waiting = waitingRepository.findById(id).orElseThrow(WaitingNotFoundException::new);
        validateIsOwner(memberId, waiting);
        deleteById(id);
    }

    private void validateIsOwner(Long memberId, Waiting waiting) {
        if (!waiting.getMember().getId().equals(memberId)) {
            throw new AccessForbiddenException();
        }
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        deleteById(id);
    }

    private void deleteById(Long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingResponse> findAll() {
        List<Waiting> waitings = waitingRepository.findAll();
        return WaitingResponse.from(waitings);
    }
}
