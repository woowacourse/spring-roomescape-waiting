package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationRequest;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationChecker reservationChecker;

    public ReservationService(ReservationRepository reservationRepository, ReservationChecker reservationChecker) {
        this.reservationRepository = reservationRepository;
        this.reservationChecker = reservationChecker;
    }

    @Transactional
    public ReservationResponse createUserReservation(UserReservationRequest dto, Member member) {
        Reservation reservation = reservationChecker.createReservationWithoutId(dto, member);
        return createReservation(reservation);
    }

    @Transactional
    public ReservationResponse createAdminReservation(ReservationRequest dto) {
        Reservation reservation = reservationChecker.createReservationWithoutId(dto);
        return createReservation(reservation);
    }

    @Transactional
    private ReservationResponse createReservation(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())) {
            throw new DuplicateContentException("[ERROR] 해당 날짜와 테마로 이미 예약된 내역이 존재합니다.");
        }
        Reservation newReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(newReservation, newReservation.getTime(), newReservation.getTheme());
    }

    public List<ReservationResponse> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(), reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponse> searchReservations(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        List<Reservation> searchResults = reservationRepository.findByMemberIdAndThemeIdAndDateRange(memberId, themeId, from, to);

        return searchResults.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(), reservation.getTheme()))
                .toList();
    }

    @Transactional
    public Reservation deleteReservation(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        reservationRepository.deleteById(id);
        return reservation.get();
    }
}
