package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.MemberNotFoundException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            TimeSlotRepository timeSlotRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository, WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findReservations(ReservationCondition cond) {
        List<Reservation> filteredReservations = reservationRepository.findByCondition(cond);
        return filteredReservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse createReservation(Long memberId, Long timeId, Long themeId, LocalDate date) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeId).orElseThrow(TimeSlotNotFoundException::new);
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Reservation reservation = Reservation.builder()
                .date(date)
                .member(member)
                .theme(theme)
                .timeSlot(timeSlot).build();

        reservation.validateDateTime();
        validateDuplicate(date, timeSlot, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateDuplicate(LocalDate date, TimeSlot time, Theme theme) {
        if (reservationRepository.findByDateAndTimeSlotAndTheme(date, time, theme).isPresent()) {
            throw new ExistedReservationException();
        }
    }

    @Transactional
    public void cancelReservationAndPromoteWait(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservationRepository.delete(reservation);
        waitingRepository.findFirstByDateAndTimeSlotAndThemeOrderById(reservation.getDate(), reservation.getTimeSlot(),
                        reservation.getTheme())
                .ifPresent(this::promoteWaitingToReservation);
    }

    private void promoteWaitingToReservation(Waiting waiting) {
        Reservation newReservation = waiting.convertToReservation();
        reservationRepository.save(newReservation);
        waitingRepository.delete(waiting);
    }

    public List<ReservationWithStatusResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationWithStatusResponse::from)
                .toList();
    }
}
