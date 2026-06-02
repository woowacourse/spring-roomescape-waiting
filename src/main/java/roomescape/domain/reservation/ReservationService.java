package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findByIdForUpdate(request.timeId())
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

        try {
            Reservation saved = reservationRepository.save(reservation);
            return ReservationResponse.from(saved);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION_NAME);
        }
    }

    @Transactional(readOnly = true)
    public List<TimeResponse> getReservations(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Long> bookedTimeIds = reservationRepository.findTimeByDateAndThemeId(date, themeId);

        return reservationTimes.stream()
                .filter(reservationTime -> !bookedTimeIds.contains(reservationTime.getId()))
                .map(TimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));

        reservationRepository.deleteById(id);

        waitingRepository.findFirstByDateAndTimeIdAndThemeIdForUpdate(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        ).ifPresent(this::promoteToReservation);
    }

    private void promoteToReservation(Waiting waiting) {
        waitingRepository.deleteById(waiting.getId());
        reservationRepository.save(Reservation.of(
                waiting.getName(), waiting.getDate(), waiting.getTime(), waiting.getTheme()
        ));
    }

    @Transactional(readOnly = true)
    public MyReservationsResponse getMyReservations(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return MyReservationsResponse.from(reservations);
    }

    @Transactional
    public void updateMyReservation(Long id, ReservationFixRequest fixRequest) {
        ReservationTime newTime = reservationTimeRepository.findByIdForUpdate(fixRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        newTime.validateIfTimePast(fixRequest.date());

        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        validateDuplicateReservation(fixRequest.date(), fixRequest.timeId(), reservation.getTheme().getId());

        reservation.validateOwner(fixRequest.name());

        try {
            reservationRepository.updateDateAndTime(id, fixRequest.date(), fixRequest.timeId());
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION_NAME);
        }
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        boolean isDuplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

}
