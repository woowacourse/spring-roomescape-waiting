package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.exception.local.PastReservationCreationException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository, UserRepository userRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllReservationsByMember(long userId) {
        Member savedMember = getUserById(userId);
        List<Reservation> reservations = reservationRepository.findByMember(savedMember);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findReservationsByFilter(
            long userId, long themeId, LocalDate from, LocalDate to
    ) {
        List<Reservation> reservations = reservationRepository.findReservationsByFilter(userId, themeId, from, to);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse addReservation(long userId, ReservationCreationContent request) {
        Member member = getUserById(userId);
        Theme theme = getThemeById(request.themeId());
        ReservationTime time = getReservationTimeById(request.timeId());

        Reservation reservation = Reservation.createWithoutId(
                request.date(), ReservationStatus.BOOKED, time, theme, member);

        validateDuplicateReservation(theme, request.date(), time);
        validatePastReservationCreation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservationById(long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        reservationRepository.delete(reservation);
    }

    private void validateDuplicateReservation(Theme theme, LocalDate date, ReservationTime time) {
        boolean isDuplicatedReservation =
                reservationRepository.existsByThemeAndDateAndReservationTime(theme, date, time);
        if (isDuplicatedReservation) {
            throw new DuplicateReservationException();
        }
    }

    private void validatePastReservationCreation(Reservation reservation) {
        if (reservation.isPastDateTime()) {
            throw new PastReservationCreationException();
        }
    }

    private Member getUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
    }

    private Theme getThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }

    private ReservationTime getReservationTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    private Reservation getReservationById(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(NotFoundReservationException::new);
    }
}
