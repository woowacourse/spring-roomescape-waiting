package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaMemberRepository jpaMemberRepository;
    private final JpaThemeRepository jpaThemeRepository;

    public ReservationService(JpaReservationRepository reservationRepository,
                              JpaReservationTimeRepository reservationTimeRepository,
                              JpaMemberRepository jpaMemberRepository, JpaThemeRepository jpaThemeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.jpaMemberRepository = jpaMemberRepository;
        this.jpaThemeRepository = jpaThemeRepository;
    }

    public List<ReservationResponse> findAllReservations(ReservationSearchParams request) {
        return reservationRepository.findByMember_IdAndTheme_IdAndDateBetween(
                        request.memberId(),
                        request.themeId(),
                        request.dateFrom(),
                        request.dateTo()
                )
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse createReservation(ReservationCreate reservationInfo) {
        Reservation reservation = reservationInfo.toReservation();
        ReservationTime time = reservationTimeRepository.findById(reservation.getTimeId())
                .orElseThrow(() -> new IllegalArgumentException("예약 하려는 시간이 저장되어 있지 않습니다."));
        ;
        validatePreviousDate(reservation, time);

        if (reservationRepository.existsByDateAndTime_IdAndTheme_Id(reservation.getDate(), reservation.getTimeId(),
                reservation.getThemeId())) {
            throw new IllegalArgumentException("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
        }

        Member member = jpaMemberRepository.fetchById(reservation.getMemberId());
        Theme theme = jpaThemeRepository.fetchById(reservation.getThemeId());
        LocalDate date = reservation.getDate();
        Reservation reservation1 = new Reservation(null, member, theme, date, time);
        Reservation savedReservation = reservationRepository.save(reservation1);
        return new ReservationResponse(savedReservation);
    }

    private void validatePreviousDate(Reservation reservation, ReservationTime time) {
        if (reservation.getDate().atTime(time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
        }
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 아이디입니다.");
        }
        reservationRepository.deleteById(id);
    }
}
