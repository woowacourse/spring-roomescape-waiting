package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.business.ReservationWithBookStateDto;
import roomescape.dto.request.ReservationCreationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.NotFoundUserException;
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

    public List<ReservationWithBookStateDto> findAllReservationsByMember(User member) {
        validateNotExistenceUser(member.getId());
        List<Reservation> reservations = reservationRepository.findByUser(member);
        return reservations.stream()
                .map(ReservationWithBookStateDto::new)
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

    public ReservationResponse addReservation(long userId, ReservationCreationRequest request) {
        User user = loadUserById(userId);
        Theme theme = loadThemeById(request.themeId());
        ReservationTime time = loadReservationTimeById(request.timeId());
        validateDuplicateReservation(request.date(), time);
        Reservation reservation = Reservation.createWithoutId(
                request.date(), ReservationStatus.BOOKED, time, theme, user);
        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservationById(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    private void validateDuplicateReservation(LocalDate date, ReservationTime time) {
        boolean isDuplicatedReservation = reservationRepository.existsByDateAndReservationTime(date, time);
        if (isDuplicatedReservation) {
            throw new DuplicateReservationException();
        }
    }

    private void validateNotExistenceUser(long userId) {
        boolean isExistUser = userRepository.existsById(userId);
        if (!isExistUser) {
            throw new NotFoundUserException();
        }
    }

    private User loadUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
    }

    private Theme loadThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(NotFoundThemeException::new);
    }

    private ReservationTime loadReservationTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(NotFoundReservationTimeException::new);
    }
}
