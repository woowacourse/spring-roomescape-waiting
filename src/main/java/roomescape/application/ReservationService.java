package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.ReservationSpecifications;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final EntityLookupService entityLookupService;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final WaitingRepository waitingRepository,
            final EntityLookupService entityLookupService
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.entityLookupService = entityLookupService;
    }

    public Reservation saveReservation(final long userId,
                                       final LocalDate date,
                                       final long timeId,
                                       final long themeId) {

        User user = entityLookupService.getUserById(userId);
        TimeSlot timeSlot = entityLookupService.getTimeSlotById(timeId);
        Theme theme = entityLookupService.getThemeById(themeId);
        validateDuplicateReservation(date, timeSlot, theme);

        Reservation reservation = Reservation.register(user, date, timeSlot, theme);

        return reservationRepository.save(reservation);
    }

    private void validateDuplicateReservation(final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        Optional<Reservation> reservation =
                reservationRepository.findByDateAndTimeSlotIdAndThemeId(date, timeSlot.id(), theme.id());

        if (reservation.isPresent()) {
            throw new AlreadyExistedException("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
        }
    }

    public List<Reservation> findReservationsByFilter(ReservationSearchFilter filter) {
        return reservationRepository.findAll(ReservationSpecifications.byFilter(filter));
    }

    @Transactional
    public void removeById(final long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        approveNextWaitingIfExists(reservation);

        reservationRepository.deleteById(id);
    }

    private void approveNextWaitingIfExists(Reservation reservation) {
        Optional<Waiting> nextWaitingOpt =
                waitingRepository.findFirstByDateAndTimeSlotIdAndThemeIdOrderByIdAsc(
                        reservation.date(),
                        reservation.timeSlot().id(),
                        reservation.theme().id());

        nextWaitingOpt.ifPresent(nextWaiting -> {
            Reservation approvedReservation = Reservation.fromWaiting(nextWaiting);
            reservationRepository.save(approvedReservation);
            waitingRepository.deleteById(nextWaiting.id());
        });
    }
}
