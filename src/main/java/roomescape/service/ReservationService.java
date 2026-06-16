package roomescape.service;

import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.ReservationUpdateRequest;
import roomescape.dto.response.ReservationRankResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

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

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    public List<ReservationRankResponse> find(String name) {
        return reservationRepository.findByName(name)
                .stream()
                .map(ReservationRankResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse save(ReservationRequest request) {
        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        Reservations reservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(
                request.date(), theme.getId(), time.getId());
        reservations.validateDuplicate(request.name());

        ReservationStatus status = reservations.determineStatus();

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                time,
                theme,
                status
        );

        if (status == ReservationStatus.CONFIRMED) {
            String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
            reservation.setPaymentInfo(orderId, 50_000L); // 데모용 고정 금액
        }

        reservation.validateNotPast();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getReservation(id);

        if (reservation.isSameDateTime(request.date(), request.timeId())) {
            return ReservationResponse.from(reservation);
        }

        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = reservation.getTheme();
        LocalDate currentDate = reservation.getDate();
        Long currentTimeId = reservation.getTime().getId();
        LocalDate newDate = request.date();
        Long newTimeId = request.timeId();

        Reservations currentReservations;
        Reservations newReservations;

        if (isBefore(currentDate, currentTimeId, newDate, newTimeId)) {
            currentReservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(currentDate, theme.getId(),
                    currentTimeId);
            newReservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(newDate, theme.getId(),
                    newTimeId);
        } else {
            newReservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(newDate, theme.getId(),
                    newTimeId);
            currentReservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(currentDate, theme.getId(),
                    currentTimeId);
        }

        newReservations.validateDuplicate(reservation.getName());

        Reservation newReservation = buildUpdatedReservation(reservation, newDate, time, newReservations);

        return ReservationResponse.from(applyUpdate(id, reservation, currentReservations, newReservation));
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = getReservation(id);

        if (reservation.isToday()) {
            throw new roomescape.exception.InvalidStateException("당일 예약은 취소할 수 없습니다.");
        }

        Reservations currentReservations = reservationRepository.findByDateAndThemeAndTimeForUpdate(
                reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId());

        reservationRepository.delete(id);
        promoteNextWaiting(reservation, currentReservations);
    }

    private boolean isBefore(LocalDate date1, Long timeId1, LocalDate date2, Long timeId2) {
        if (date1.isBefore(date2)) {
            return true;
        }
        if (date1.isAfter(date2)) {
            return false;
        }
        return timeId1 < timeId2;
    }

    private Reservation buildUpdatedReservation(Reservation origin, LocalDate newDate, ReservationTime newTime,
                                                Reservations newReservations) {
        Reservation newReservation = new Reservation(
                origin.getName(),
                newDate,
                newTime,
                origin.getTheme(),
                newReservations.determineStatus()
        );
        newReservation.validateNotPast();
        return newReservation;
    }

    private Reservation applyUpdate(Long id, Reservation deleted, Reservations currentReservations,
                                    Reservation newReservation) {
        reservationRepository.delete(id);
        promoteNextWaiting(deleted, currentReservations);
        return reservationRepository.save(newReservation);
    }

    private void promoteNextWaiting(Reservation deleted, Reservations currentReservations) {
        if (deleted.takesSlot()) {
            currentReservations.findNextWaiting(deleted.getId())
                    .ifPresent(next -> {
                        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
                        next.setPaymentInfo(orderId, 50_000L); // 결제 대기 상태로 변경 및 주문 정보 설정
                        reservationRepository.updatePayment(next.getId(), null, next.getStatus(), next.getOrderId(), next.getAmount());
                    });
        }
    }

    private Reservation getReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("요청하신 예약을 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findTimeById(timeId)
                .orElseThrow(() -> new NotFoundException("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요."));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findThemeById(themeId)
                .orElseThrow(() -> new NotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요."));
    }
}
