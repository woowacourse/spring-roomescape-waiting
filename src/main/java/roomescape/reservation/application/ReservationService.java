package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AdminReservationSearchRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.UserReservationRequest;
import roomescape.reservation.application.event.ReservationDeletedEvent;
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
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.domain.Waitings;

@Service
@AllArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<Waiting> myWaitings = waitingRepository.findByMemberId(memberId);
        List<WaitingWithRank> rankedWaitings = getWaitingWithRanks(myWaitings);
        return MyReservationResponse.of(reservations, rankedWaitings);
    }

    private List<WaitingWithRank> getWaitingWithRanks(List<Waiting> myWaitings) {
        return myWaitings.stream()
                .map(waiting -> {
                    Waitings waitings = new Waitings(waitingRepository.findBySpec(waiting.getSpec()));
                    long rank = waitings.getRankOf(waiting);
                    return new WaitingWithRank(waiting, rank);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findFiltered(AdminReservationSearchRequest request) {
        Long memberId = request.memberId();
        Long themeId = request.themeId();
        LocalDate from = request.from();
        LocalDate to = request.to();

        return ReservationResponse.from(reservationRepository.findFiltered(memberId, themeId, from, to));
    }

    public ReservationResponse createByUser(Long memberId, UserReservationRequest request) {
        return create(memberId, request.date(), request.timeId(), request.themeId());
    }

    public ReservationResponse createByAdmin(AdminReservationRequest request) {
        return create(request.memberId(), request.date(), request.timeId(), request.themeId());
    }

    private ReservationResponse create(Long memberId, LocalDate dateInput, Long timeId, Long themeId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        ReservationDate date = new ReservationDate(dateInput);
        ReservationTime time = timeRepository.findById(timeId).orElseThrow(TimeNotFoundException::new);
        validateInPast(date, time);

        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);

        ReservationSpec spec = new ReservationSpec(date, time, theme);
        validateDuplicated(spec);

        Reservation reservation = new Reservation(member, spec);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    private void validateDuplicated(ReservationSpec spec) {
        if (reservationRepository.existsBySpec(spec)) {
            throw new ReservationAlreadyExistsException();
        }
    }

    private void validateInPast(ReservationDate date, ReservationTime time) {
        if (date.isInPast() || date.isToday() && time.isBeforeNow()) {
            throw new ReservationInPastException();
        }
    }

    public void deleteById(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        reservationRepository.deleteById(id);
        eventPublisher.publishEvent(new ReservationDeletedEvent(reservation));
    }
}
