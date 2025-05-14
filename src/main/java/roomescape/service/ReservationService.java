package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSpecification;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationChecker reservationChecker;

    public ReservationService(ReservationRepository reservationRepository, ReservationChecker reservationChecker) {
        this.reservationRepository = reservationRepository;
        this.reservationChecker = reservationChecker;
    }

    public ReservationResponse createUserReservation(UserReservationRequest dto, Member member) {
        Reservation reservation = reservationChecker.createReservationWithoutId(dto, member);
        return createReservation(reservation);
    }

    public ReservationResponse createAdminReservation(ReservationRequest dto) {
        Reservation reservation = reservationChecker.createReservationWithoutId(dto);
        return createReservation(reservation);
    }

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

    public List<ReservationResponse> searchReservations(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        Specification<Reservation> specification = ReservationSpecification.getReservationSpecification(themeId, memberId, from, to);
        List<Reservation> searchResults = reservationRepository.findAll(specification);

        return searchResults.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(), reservation.getTheme()))
                .toList();
    }

    public void deleteReservation(Long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        reservationRepository.deleteById(id);
    }
}
