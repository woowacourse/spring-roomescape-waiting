package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.timeslot.TimeSlotRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.ReservationSpecifications;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final WaitingRepository waitingRepository,
            final TimeSlotRepository timeSlotRepository,
            final ThemeRepository themeRepository,
            final UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    public Reservation saveReservation(final User user,
                                       final LocalDate date,
                                       final long timeId,
                                       final long themeId) {

        TimeSlot timeSlot = getTimeSlotById(timeId);
        Theme theme = getThemeById(themeId);
        validateDuplicateReservation(date, timeSlot, theme);

        Reservation reservation = Reservation.reserveNewly(user, date, timeSlot, theme);
        return reservationRepository.save(reservation);
    }

    private TimeSlot getTimeSlotById(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 타임 슬롯입니다."));
    }

    private Theme getThemeById(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
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

    public List<Reservation> findReservationsByUserId(final long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + userId));

        return reservationRepository.findByUserId(user.id());
    }

    public void removeById(final long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        Optional<Waiting> waiting =
                waitingRepository.findFirstByDateAndTimeSlotIdAndThemeIdOrderByIdAsc(
                        reservation.date(),
                        reservation.timeSlot().id(),
                        reservation.theme().id());

        waiting.ifPresent(firstWaiting -> {
            Reservation waitingReservation = Reservation.fromWaiting(firstWaiting);
            reservationRepository.save(waitingReservation);
            waitingRepository.deleteById(firstWaiting.id());
        });

        reservationRepository.deleteById(id);
    }
}
