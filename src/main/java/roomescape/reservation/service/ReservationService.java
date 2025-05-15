package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.util.TokenProvider;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationChecker reservationChecker;
    private final TokenProvider tokenProvider;

    public ReservationService(ReservationRepository reservationRepository, ReservationChecker reservationChecker,
                              TokenProvider tokenProvider) {
        this.reservationRepository = reservationRepository;
        this.reservationChecker = reservationChecker;
        this.tokenProvider = tokenProvider;
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
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())) {
            throw new DuplicateContentException("[ERROR] 해당 날짜와 테마로 이미 예약된 내역이 존재합니다.");
        }
        Reservation newReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(newReservation, newReservation.getTime(), newReservation.getTheme());
    }

    public List<ReservationResponse> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponse> searchReservations(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        List<Reservation> searchResults = reservationRepository.findByMemberIdAndThemeIdAndDateRange(memberId, themeId,
                from, to);

        return searchResults.stream()
                .map(reservation -> ReservationResponse.from(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public void deleteReservation(Long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NotFoundException("[ERROR] 등록된 예약만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }

        reservationRepository.deleteById(id);
    }

    public List<MemberReservationResponse> findAllMemberReservations(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return reservations.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
