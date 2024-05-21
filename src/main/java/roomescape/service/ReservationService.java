package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationDuplicatedException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.ReservationWaitingNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.repository.dto.ReservationWaitingResponse;
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
    public List<ReservationResponse> searchConfirmedReservations(ReservationSearchParams request) {
        Specification<Reservation> specification = getSearchSpecification(request);
        return reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationWaitingResponse> findAllWaitingReservations() {
        return reservationRepository.findReservationByStatus(WAITING);
    }

    @Transactional(readOnly = true)
    public List<ReservationRankResponse> findReservationsByMemberEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        return reservationRepository.findReservationRankByMember(member);
    }

    public ReservationResponse createReservation(ReservationCreate createInfo) {
        LocalDate date = createInfo.getDate();
        Member member = memberRepository.findByEmail(createInfo.getEmail()).orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.fetchById(createInfo.getThemeId());
        ReservationTime time = reservationTimeRepository.fetchById(createInfo.getTimeId());

        validatePreviousDate(date, time);
        validateDuplicatedReservation(member, theme, date, time);

        Reservation reservation = reservationRepository.save(generateReservation(theme, date, time, member));
        return new ReservationResponse(reservation);
    }

    public void deleteWaitingReservation(String email, long id) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        if (!reservationRepository.existsByIdAndMemberAndStatus(id, member, WAITING)) {
            throw new ReservationWaitingNotFoundException();
        }
        reservationRepository.deleteById(id);
    }

    public void deleteConfirmedReservation(long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservationRepository.deleteById(id);
        Optional<Reservation> first = reservationRepository.findFirstByThemeAndDateAndTimeAndStatus(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                WAITING
        );
        first.ifPresent(value -> value.updateStatus(CONFIRMED));
    }

    public void deleteWaitingReservation(long id) {
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

    private Reservation generateReservation(Theme theme, LocalDate date, ReservationTime time, Member member) {
        if (reservationRepository.existsByThemeAndDateAndTime(theme, date, time)) {
            return new Reservation(member, theme, date, time, WAITING);
        }
        return new Reservation(member, theme, date, time, CONFIRMED);
    }

    private Specification<Reservation> getSearchSpecification(ReservationSearchParams request) {
        return ((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            root.fetch("member");
            root.fetch("theme");
            root.fetch("time");

            if (request.getEmail() != null && !request.getEmail().isBlank()) {
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
            if (request.getStatus() != null) {
                predicates.add(builder.equal(root.get("status"), request.getStatus()));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
