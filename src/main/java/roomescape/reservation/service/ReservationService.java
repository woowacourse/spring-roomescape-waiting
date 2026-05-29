package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
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
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            ReservationFactory reservationFactory,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationFactory = reservationFactory;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        if (reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        Reservation saved = reservationRepository.save(
                reservationFactory.create(request.name(), request.date(), time, theme));
        return ReservationResponse.from(saved);
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_CANCEL);
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_RESERVATION_UPDATE);
        }
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                reservation.getTheme().getId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }
        reservationRepository.update(id, request.date(), request.timeId());
        return ReservationResponse.from(getById(id));
    }

    @NonNull
    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public ReservationIdResponse getReservationId(LocalDate date, Long themeId, Long timeId) {
        return reservationRepository.findReservationId(date, themeId, timeId);
    }
}
