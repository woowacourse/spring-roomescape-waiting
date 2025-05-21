package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationQueryService {

    private final JpaReservationRepository reservationRepository;

    public ReservationQueryService(final JpaReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from,
                                                               LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findByPeriod(from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<MyReservationResponseDto> findMyReservations(LoginInfo loginInfo) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(loginInfo.id());
        return reservations.stream().map(reservation -> new MyReservationResponseDto(reservation)).toList();
    }
}
