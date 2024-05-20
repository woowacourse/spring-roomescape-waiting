package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.member.UnauthorizedEmailException;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationDuplicatedException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.ReservationWaitingNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

@Service
@Transactional
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

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservations(ReservationSearchParams request) {
        Specification<Reservation> specification = getSearchSpecification(request);

        return reservationRepository.findAll(specification)
                .stream().map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationRankResponse> findReservationsByMemberEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(UnauthorizedEmailException::new);
        return reservationRepository.findReservationRankByMember(member);
    }

    public ReservationResponse createReservation(ReservationCreate createInfo) {
        LocalDate date = createInfo.getDate();
        Member member = memberRepository.findByEmail(createInfo.getEmail()).orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.fetchById(createInfo.getThemeId());
        ReservationTime time = reservationTimeRepository.fetchById(createInfo.getTimeId());

        validatePreviousDate(date, time);
        validateDuplicatedReservation(member, theme, date, time);

        Reservation reservation = generateReservation(theme, date, time, member);
        return new ReservationResponse(reservationRepository.save(reservation));
    }

    private Reservation generateReservation(Theme theme, LocalDate date, ReservationTime time, Member member) {
        if (reservationRepository.existsByThemeAndDateAndTimeAndReservationStatus(theme, date, time, WAITING)) {
            return new Reservation(member, theme, date, time, WAITING);
        }
        return new Reservation(member, theme, date, time, RESERVED);
    }

    public void deleteReservationWaiting(String email, long id) {
        if (!memberRepository.existsByEmail(email)) {
            throw new MemberNotFoundException();
        }
        if (!reservationRepository.existsByIdAndReservationStatus(id, WAITING)) {
            throw new ReservationWaitingNotFoundException();
        }
        reservationRepository.deleteById(id);
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

    private void validateDuplicatedReservation(Member member, Theme theme, LocalDate date, ReservationTime time) {
        if (reservationRepository.existsByMemberAndThemeAndDateAndTime(member, theme, date, time)) {
            throw new ReservationDuplicatedException();
        }
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
}
