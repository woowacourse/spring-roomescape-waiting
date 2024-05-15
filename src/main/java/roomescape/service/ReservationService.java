package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationConflictException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

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
        Specification<Reservation> specification = request.getSearchSpecification();

        return reservationRepository.findAll(specification)
                .stream().map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findReservationsByMemberEmail(String email) {
        return reservationRepository.findAllByMemberEmail(email)
                .stream().map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse createReservation(ReservationCreate reservationInfo) {
        long timeId = reservationInfo.getTimeId();
        LocalDate date = reservationInfo.getDate();
        long themeId = reservationInfo.getThemeId();
        String email = reservationInfo.getEmail();

        ReservationTime time = reservationTimeRepository.fetchById(timeId);
        validatePreviousDate(date, time);
        validateDuplicatedReservation(date, themeId, timeId);

        Member member = jpaMemberRepository.fetchByEmail(email);
        Theme theme = jpaThemeRepository.fetchById(themeId);
        Reservation savedReservation = reservationRepository.save(new Reservation(member, theme, date, time));
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException();
        }
        reservationRepository.deleteById(id);
    }

    private void validatePreviousDate(LocalDate date, ReservationTime time) {
        if (date.atTime(time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new DateTimePassedException();
        }
    }

    private void validateDuplicatedReservation(LocalDate date, Long themeId, Long timeId) {
        if (reservationRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)) {
            throw new ReservationConflictException();
        }
    }
}
