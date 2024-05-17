package roomescape.service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationConflictException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.reservation.MyReservationResponse;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              MemberRepository memberRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAllReservations(ReservationSearchParams request) {
        Specification<Reservation> specification = getSearchSpecification(request);

        return reservationRepository.findAll(specification)
                .stream().map(ReservationResponse::new)
                .toList();
    }

    public List<MyReservationResponse> findReservationsByMemberEmail(String email) {
        return reservationRepository.findAllByMemberEmail(email)
                .stream().map(MyReservationResponse::new)
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

        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.fetchById(themeId);
        Reservation savedReservation = reservationRepository.save(new Reservation(member, theme, date, time));
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException();
        }
        reservationRepository.deleteById(id);
    }

    private Specification<Reservation> getSearchSpecification(ReservationSearchParams request) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            root.fetch("member");
            root.fetch("theme");
            root.fetch("time");

            if (request.getEmail() != null) {
                predicates.add(builder.equal(root.get("member").get("email"), request.getEmail()));
            }
            if (request.getThemeId() != null) {
                predicates.add(builder.equal(root.get("theme").get("id"), request.getThemeId()));
            }
            if (request.getDateFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), request.getDateFrom()));
            }
            if (request.getDateTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), request.getDateTo()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
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
