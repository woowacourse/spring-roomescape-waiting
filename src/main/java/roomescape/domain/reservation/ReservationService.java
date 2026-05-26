package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.domain.theme.Theme;
import roomescape.admin.theme.AdminThemeRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.reservationtime.ReservationTimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        validateDuplicateReservation(request.date(), request.timeId(), request.themeId());
        time.validateIfTimePast(request.date());

        Reservation reservation = Reservation.of(
            request.name(),
            request.date(),
            time,
            theme
        );
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.of(saved);
    }

    public List<TimeResponse> getReservations(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Long> bookedTimeIds = reservationRepository.findTimeByDateAndThemeId(date, themeId);

        return reservationTimes.stream()
            .filter(reservationTime -> !bookedTimeIds.contains(reservationTime.getId()))
            .map(TimeResponse::of)
            .toList();
    }

    public void deleteReservation(Long id) {
        validateReservationId(id);
        reservationRepository.deleteById(id);
    }

    public MyReservationsResponse getMyReservations(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return MyReservationsResponse.from(reservations);
    }

    public void updateMyReservation(Long id, ReservationFixRequest fixRequest) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        validateFixRequest(reservation.getTheme(), fixRequest);
        reservationTimeRepository.findById(fixRequest.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));

        reservation.validateOwner(fixRequest.name());

        reservationRepository.updateDateAndTime(id, fixRequest.date(), fixRequest.timeId());
    }

    private void validateReservationId(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND);
        }
    }

    private void validateFixRequest(Theme theme, ReservationFixRequest newRequest) {
        if (!reservationTimeRepository.existsById(newRequest.timeId())) {
            throw new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND);
        }
        ReservationTime newTime = reservationTimeRepository.findById(newRequest.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        newTime.validateIfTimePast(newRequest.date());
        validateDuplicateReservation(newRequest.date(), newRequest.timeId(), theme.getId());
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        boolean isDuplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

}
