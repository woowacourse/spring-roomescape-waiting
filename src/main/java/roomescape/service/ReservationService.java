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
import roomescape.service.exception.DateTimePassedException;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;

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
        return reservationRepository.findByMemberIdAndThemeIdAndDateBetween(
                        request.memberId(),
                        request.themeId(),
                        request.dateFrom(),
                        request.dateTo())
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse createReservation(ReservationCreate reservationInfo) {
        Reservation reservation = reservationInfo.toReservation();
        Long timeId = reservation.timeId();

        ReservationTime time = reservationTimeRepository.fetchById(timeId);
        validatePreviousDate(reservation, time);

        Long themeId = reservation.themeId();
        LocalDate date = reservation.getDate();
        validateDuplicatedReservation(date, themeId, timeId);

        Member member = jpaMemberRepository.fetchById(reservation.memberId());
        Theme theme = jpaThemeRepository.fetchById(themeId);
        Reservation savedReservation = reservationRepository.save(new Reservation(member, theme, date, time));
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException("존재하지 않는 아이디입니다.");
        }
        reservationRepository.deleteById(id);
    }

    private void validatePreviousDate(Reservation reservation, ReservationTime time) {
        if (reservation.getDate().atTime(time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new DateTimePassedException("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long themeId, Long timeId) {
        if (reservationRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)) {
            throw new ReservationConflictException("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
        }
    }
}
