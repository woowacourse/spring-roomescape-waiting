package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.DuplicateReservationException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.BookingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
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

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationWaitingRepository reservationWaitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public BookingResponse createReservation(Member member, ReservationRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());
        LocalDate date = request.date();
        Long timeId = request.timeId();
        Long themeId = request.themeId();

        reservationRepository.lockSlot(date, timeId, themeId);
        validateNotAlreadyBooked(member.getId(), date, timeId, themeId);

        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            ReservationWaiting waiting = reservationWaitingRepository.save(
                    ReservationWaiting.of(member, date, time, theme));
            return BookingResponse.waiting(waiting);
        }

        Reservation saved = reservationRepository.save(Reservation.of(member, date, time, theme));
        return BookingResponse.reserved(saved);
    }

    private void validateNotAlreadyBooked(Long memberId, LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 예약한 슬롯입니다.");
        }
        if (reservationWaitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(memberId, date, timeId, themeId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 대기 중인 슬롯입니다.");
        }
    }

    public List<ReservationResponse> getReservationsByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long id, Long memberId) {
        Reservation reservation = getById(id);
        if (!reservation.isOwnedBy(memberId)) {
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
                    reservationRepository.save(Reservation.of(
                            waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme()));
                });
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
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
        return ReservationResponse.from(changed);
    }

    @NonNull
    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

}
