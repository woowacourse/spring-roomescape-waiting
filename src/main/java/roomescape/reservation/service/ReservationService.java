package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService,
                              ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public ReservationResult save(ReservationCommand command, LocalDateTime requestTime) {
        try {
            Reservation saved = reservationRepository.save(buildNewReservation(command, requestTime));
            return ReservationResult.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void update(ReservationUpdateCommand command, Long id, String name, LocalDateTime requestTime) {
        Reservation updated = buildUpdatedReservation(command, id, name, requestTime);
        try {
            reservationRepository.save(updated);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void deleteById(Long id, String name, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        if (name != null) {
            reservation.validateOwner(name);
        }
        reservation.validateExpiry(requestTime);

        reservationTimeService.getByIdForUpdate(reservation.getTimeId());

        List<ReservationWaiting> waitings = reservationWaitingRepository.queryAllBySlotForUpdate(
                new ReservationSlot(reservation.getDate(), reservation.getTime(), reservation.getTheme())
        );

        reservationRepository.delete(reservation);
        promoteNextWaiting(waitings, requestTime);
    }

    public List<ReservationResult> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationWithStatusResult> findAllByName(String name) {
        // 1. 유저의 예약 내역 조회
        List<Reservation> reservations = reservationRepository.findAllByName(name);
        List<ReservationWithStatusResult> reservationResults = reservations.stream()
                .map(r -> new ReservationWithStatusResult(
                        r.getId(),
                        r.getName(),
                        r.getDate(),
                        r.getTime(),
                        r.getTheme(),
                        "reserved",
                        0L
                ))
                .toList();

        // 2. 유저의 대기 내역 조회
        List<ReservationWaiting> userWaitings = reservationWaitingRepository.findAllByName(name);
        if (userWaitings.isEmpty()) {
            return reservationResults;
        }

        // 3. 유저가 대기 중인 슬롯들의 전체 대기자 목록 일괄 조회 (Batch Query)
        List<ReservationSlot> slots = userWaitings.stream()
                .map(ReservationWaiting::getSlot)
                .toList();
        List<ReservationWaiting> allWaitingsForSlots = reservationWaitingRepository.findAllBySlots(slots);

        // 4. 슬롯별로 전체 대기자 목록 그룹화
        Map<ReservationSlot, List<ReservationWaiting>> waitingsBySlot = allWaitingsForSlots.stream()
                .collect(Collectors.groupingBy(ReservationWaiting::getSlot));

        // 5. 각 슬롯의 대기자 목록을 정렬 정책에 따라 정렬하고 사용자의 순번 계산
        List<ReservationWithStatusResult> waitingResults = new ArrayList<>();
        for (ReservationWaiting waiting : userWaitings) {
            List<ReservationWaiting> slotQueue = new ArrayList<>(waitingsBySlot.getOrDefault(waiting.getSlot(), List.of()));
            
            // ID 기준으로 정렬 (FIFO 정책)
            slotQueue.sort(Comparator.comparing(ReservationWaiting::getId));

            // 유저의 대기 순번 계산 (index + 1)
            long rank = slotQueue.indexOf(waiting) + 1;

            waitingResults.add(new ReservationWithStatusResult(
                    waiting.getId(),
                    waiting.getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme(),
                    "waiting",
                    rank
            ));
        }

        // 6. 예약과 대기 내역을 합쳐서 정렬 반환
        List<ReservationWithStatusResult> combined = new ArrayList<>();
        combined.addAll(reservationResults);
        combined.addAll(waitingResults);
        
        combined.sort((r1, r2) -> {
            int dateCompare = r1.date().compareTo(r2.date());
            if (dateCompare != 0) {
                return dateCompare;
            }
            int timeCompare = r1.time().getStartAt().compareTo(r2.time().getStartAt());
            if (timeCompare != 0) {
                return timeCompare;
            }
            int themeCompare = r1.theme().getId().compareTo(r2.theme().getId());
            if (themeCompare != 0) {
                return themeCompare;
            }
            return r1.status().compareTo(r2.status());
        });

        return combined;
    }

    public PopularThemesResult queryPopularThemes(int period, int limit) {
        int oneDayDifference = 1;
        LocalDate to = LocalDate.now().minusDays(oneDayDifference);
        LocalDate from = to.minusDays(period).plusDays(oneDayDifference);
        return new PopularThemesResult(
                reservationRepository.queryPopularThemes(from, to, limit)
        );
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );
    }

    private Reservation buildNewReservation(ReservationCommand command, LocalDateTime requestTime) {
        ReservationTime time = reservationTimeService.getByIdForUpdate(command.timeId());
        Theme theme = themeService.findById(command.themeId());

        Reservation newReservation = new Reservation(command.name(), command.date(), time, theme, requestTime);
        validateNoDoubleBooking(newReservation);

        return newReservation;
    }

    private void validateNoDoubleBooking(Reservation newReservation) {
        validateNoSameTimeBooking(newReservation);
        validateNoSameTimeWaiting(newReservation);
    }

    private void validateNoSameTimeBooking(Reservation newReservation) {
        if (reservationRepository.hasBookingAtSameTime(newReservation)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
    }

    private void validateNoSameTimeWaiting(Reservation newReservation) {
        ReservationWaiting dummy = new ReservationWaiting(
                null,
                newReservation.getName(),
                newReservation.getSlot(),
                newReservation.getUpdatedAt()
        );
        if (reservationWaitingRepository.hasWaitingAtSameTime(dummy)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
    }

    private Reservation buildUpdatedReservation(ReservationUpdateCommand command, Long id, String name, LocalDateTime requestTime) {
        Reservation reservation = getById(id);
        ReservationTime newTime = getReservationTime(command.timeId());
        Reservation updated = reservation.update(command.date(), newTime, name, requestTime);
        validateNoDoubleBookingForUpdate(updated);
        return updated;
    }

    private ReservationTime getReservationTime(Long timeId) {
        if (timeId == null) {
            return null;
        }
        return reservationTimeService.getByIdForUpdate(timeId);
    }

    private void validateNoDoubleBookingForUpdate(Reservation updated) {
        if (reservationRepository.isAlreadyBookedByOthers(updated)) {
            throw new InvalidBusinessStateException(
                    ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME);
        }
        validateNoSameTimeWaiting(updated);
    }

    private void promoteNextWaiting(List<ReservationWaiting> lockedWaitings, LocalDateTime requestTime) {
        for (ReservationWaiting waiting : lockedWaitings) {
            Reservation candidate = new Reservation(null, waiting.getName(), waiting.getSlot(), waiting.getSlot().date().atStartOfDay());
            if (!reservationRepository.hasBookingAtSameTime(candidate)) {
                createReservationFromWaiting(waiting, requestTime);
                return;
            }
        }
    }

    private void createReservationFromWaiting(ReservationWaiting waiting, LocalDateTime requestTime) {
        reservationWaitingRepository.delete(waiting);


        Reservation newReservation = new Reservation(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme(),
                requestTime
        );
        reservationRepository.save(newReservation);
    }
}
