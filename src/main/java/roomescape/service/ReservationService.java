package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationSearchCondition;
import roomescape.domain.BookingSlot;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPolicy;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.NotFoundException;
import roomescape.service.dto.param.CreateBookingParam;
import roomescape.service.dto.result.ReservationResult;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationPolicy reservationPolicy;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                              MemberRepository memberRepository, ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ReservationPolicy reservationPolicy) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationPolicy = reservationPolicy;
    }

    public List<ReservationResult> getReservationsInConditions(ReservationSearchCondition condition) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(
                condition.memberId(),
                condition.themeId(),
                condition.dateFrom(),
                condition.dateTo()
        );

        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult create(CreateBookingParam param) {
        ReservationComponents components = loadComponents(param);
        Reservation reservation = Reservation.create(
                components.member,
                new BookingSlot(param.date(), components.time, components.theme)
        );
        validateCanReservation(reservation);
        reservationRepository.save(reservation);

        return ReservationResult.from(reservation);
    }

    private void validateCanReservation(Reservation reservation) {
        boolean existsDuplicateReservation = reservationRepository.existsDuplicateReservation(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        reservationPolicy.validateReservationAvailable(reservation, existsDuplicateReservation);
    }

    private ReservationComponents loadComponents(CreateBookingParam param) {
        ReservationTime reservationTime = reservationTimeRepository.findById(param.timeId())
                .orElseThrow(() -> new NotFoundException("timeId", param.timeId()));
        Theme theme = themeRepository.findById(param.themeId())
                .orElseThrow(() -> new NotFoundException("themeId", param.themeId()));
        Member member = memberRepository.findById(param.memberId())
                .orElseThrow(() -> new NotFoundException("memberId", param.memberId()));

        return new ReservationComponents(member, theme, reservationTime);
    }

    private record ReservationComponents(Member member, Theme theme, ReservationTime time) {
    }

    @Transactional
    public void deleteByIdAndReserveNextWaiting(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("reservationId", reservationId));

        reservationRepository.deleteById(reservationId);

        boolean reservationSlotEmpty = reservationRepository.isBookingSlotEmpty(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        if(reservationSlotEmpty) {
            autoReserveNextWaiting(reservation);
        }
    }

    private void autoReserveNextWaiting(Reservation canceled) {
        Optional<Waiting> firstWaiting = waitingRepository.findFirstWaiting(
                canceled.getDate(), canceled.getTheme().getId(), canceled.getTime().getId());

        firstWaiting.ifPresent(waiting -> {
            Reservation reservation = waiting.confirm();
            reservationRepository.save(reservation);
            waitingRepository.delete(waiting);
        });
    }
}
