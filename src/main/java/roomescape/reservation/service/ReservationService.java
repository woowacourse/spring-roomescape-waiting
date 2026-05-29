package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.DuplicateReservationException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFactory;
import roomescape.reservation.dto.ReservationIdResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationFactory reservationFactory;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationFactory reservationFactory
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationFactory = reservationFactory;
    }

    @Transactional
    public ReservationResponse createReservation(Member member, ReservationRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        if (reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())) {
            throw new DuplicateReservationException();
        }

        Reservation saved = reservationRepository.save(
                reservationFactory.create(member, request.date(), time, theme));
        return ReservationResponse.from(saved);
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
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (reservation.isPast()) {
            throw new PastTimeCancelException();
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        if (reservation.isPast()) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_UPDATE);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public ReservationIdResponse getReservationId(LocalDate date, Long themeId, Long timeId) {
        return reservationRepository.findReservationId(date, themeId, timeId);
    }
}