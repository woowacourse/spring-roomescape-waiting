package roomescape.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWaitingOrderResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationWaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository, ReservationWaitingRepository reservationWaitingRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              ReservationWaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingService = waitingService;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.date(), reservationTime, theme);

        validateUniqueReservation(slot);
        validatePastDatetime(slot, now);

        Reservation reservation = Reservation.createWithoutId(command.name(), slot);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(String name) {
        List<MyReservationResponse> reservations = reservationRepository.findByName(name).stream()
                .map(MyReservationResponse::fromReservation)
                .toList();
        List<MyReservationResponse> reservationWaitings = reservationWaitingRepository.findByNameOrderByCreatedAt(name)
                .stream()
                .map(waiting -> new ReservationWaitingOrderResponse(
                        waiting,
                        reservationWaitingRepository.countOrder(waiting.getSlot(), waiting.getId())
                ))
                .map(MyReservationResponse::fromReservationWaiting)
                .toList();

        return getMyReservationResponses(reservations, reservationWaitings);
    }

    @Transactional
    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime time = getTime(command.timeId());
        ReservationSlot slot = new ReservationSlot(command.date(), time, reservation.getTheme());

        validateUniqueExcludingSelf(slot, reservationId);
        validatePastDatetime(slot, now);

        reservation.changeSlot(slot);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void delete(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservationRepository.deleteById(reservationId);
        waitingService.promoteFirstWaiting(reservation.getSlot());
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateUniqueReservation(ReservationSlot slot) {
        boolean exists = reservationRepository.existsBySlot(slot);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueExcludingSelf(ReservationSlot slot, long id) {
        boolean exists = reservationRepository.existsBySlotAndIdNot(slot, id);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(ReservationSlot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_DATETIME);
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }

    private List<MyReservationResponse> getMyReservationResponses(List<MyReservationResponse> reservations,
                                                                  List<MyReservationResponse> reservationWaitings) {
        List<MyReservationResponse> reservationResponses = new ArrayList<>();
        reservationResponses.addAll(reservations);
        reservationResponses.addAll(reservationWaitings);
        reservationResponses.sort(
                Comparator.comparing(MyReservationResponse::date)
                        .thenComparing(reservation -> reservation.time().startAt())
                        .thenComparing(reservation -> reservation.theme().name())
        );
        return reservationResponses;
    }
}
