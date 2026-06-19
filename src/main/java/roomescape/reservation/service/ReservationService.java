package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.DuplicateReservationException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.member.domain.Member;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final PaymentService paymentService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationWaitingRepository reservationWaitingRepository,
            PaymentService paymentService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public BookingResult book(Member member, ReservationRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());
        LocalDate date = request.date();

        reservationRepository.lockSlot(date, time.getId(), theme.getId());
        validateNotAlreadyBooked(member.getId(), date, time.getId(), theme.getId());

        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, time.getId(), theme.getId())) {
            return BookingResult.waiting(saveWaiting(member, date, time, theme));
        }
        return reserveOrWaitOnConflict(member, date, time, theme);
    }

    private BookingResult reserveOrWaitOnConflict(Member member, LocalDate date, ReservationTime time, Theme theme) {
        try {
            Reservation reservation = reservationRepository.save(Reservation.pending(member, date, time, theme));
            String orderId = paymentService.prepare(reservation.getId(), theme.getPrice());
            return BookingResult.pendingPayment(reservation, orderId, theme.getPrice());
        } catch (DataIntegrityViolationException e) {
            return BookingResult.waiting(saveWaiting(member, date, time, theme));
        }
    }

    private ReservationWaiting saveWaiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return reservationWaitingRepository.save(ReservationWaiting.of(member, date, time, theme));
    }

    private void validateNotAlreadyBooked(Long memberId, LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 예약한 슬롯입니다.");
        }
        if (reservationWaitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 대기 중인 슬롯입니다.");
        }
    }

    public List<Reservation> getReservationsByMember(Member member) {
        return reservationRepository.findByMemberId(member.getId());
    }

    @Transactional
    public void deleteReservation(Long id, Member member) {
        Reservation reservation = getById(id);
        if (!reservation.isOwnedBy(member.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        if (reservation.isPast()) {
            throw new PastTimeCancelException();
        }
        reservationRepository.lockSlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
        reservationRepository.deleteById(id);
        promoteFirstWaiting(reservation);
    }

    private void promoteFirstWaiting(Reservation canceled) {
        reservationWaitingRepository.findFirstByDateAndTimeIdAndThemeId(
                        canceled.getDate(), canceled.getTime().getId(), canceled.getTheme().getId())
                .ifPresent(waiting -> {
                    reservationWaitingRepository.deleteById(waiting.getId());
                    Reservation promoted = reservationRepository.save(Reservation.pending(
                            waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme()));
                    paymentService.prepare(promoted.getId(), waiting.getTheme().getPrice());
                });
    }

    @Transactional
    public Reservation updateReservation(Long id, Member member, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        if (!reservation.isOwnedBy(member.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        if (reservation.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 예약은 변경할 수 없습니다.");
        }

        ReservationTime newTime = reservationTimeService.getById(request.timeId());
        LocalDate newDate = request.date();

        Reservation changed = reservation.reschedule(newDate, newTime);
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(newDate, request.timeId(),
                reservation.getTheme().getId())) {
            throw new DuplicateReservationException();
        }

        reservationRepository.update(id, newDate, request.timeId());
        return changed;
    }

    @NonNull
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

}
