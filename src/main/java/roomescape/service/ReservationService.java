package roomescape.service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.member.MemberNotFoundException;
import roomescape.exception.reservation.CannotWaitingForMineException;
import roomescape.exception.reservation.DateTimePassedException;
import roomescape.exception.reservation.ReservationConflictException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.exception.reservation.WaitingConflictException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;
import roomescape.service.dto.waiting.WaitingResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              MemberRepository memberRepository, ThemeRepository themeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findAllReservations(ReservationSearchParams request) {
        Specification<Reservation> specification = getSearchSpecification(request);

        return reservationRepository.findAll(specification)
                .stream().map(ReservationResponse::new)
                .toList();
    }

    public List<WaitingResponse> findWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::new)
                .toList();
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

    public List<ReservationResponse> findReservationsByMemberEmail(String email) {
        Stream<ReservationResponse> reservationsConfirm = reservationRepository.findByMemberEmail(email).stream()
                .map(ReservationResponse::new);
        Stream<ReservationResponse> reservationsWaiting = waitingRepository.findWithRankByMemberEmail(email)
                .stream()
                .map(ReservationResponse::new);

        return Stream.concat(reservationsConfirm, reservationsWaiting)
                .sorted(this::compareReservation)
                .toList();
    }

    private int compareReservation(ReservationResponse reservation1, ReservationResponse reservation2) {
        int comparedDate = reservation1.getDate().compareTo(reservation2.getDate());
        if (comparedDate != 0) {
            return comparedDate;
        }
        int comparedTime = reservation1.getTime().getStartAt().compareTo(reservation2.getTime().getStartAt());
        if (comparedTime != 0) {
            return comparedTime;
        }
        return (int) (reservation1.getTheme().getId() - reservation2.getTime().getId());
    }

    public ReservationResponse createReservation(ReservationCreate reservationInfo) {
        long timeId = reservationInfo.getTimeId();
        LocalDate date = reservationInfo.getDate();
        long themeId = reservationInfo.getThemeId();
        String email = reservationInfo.getEmail();

        ReservationTime time = reservationTimeRepository.fetchById(timeId);
        validatePreviousDate(date, time);
        validateDuplicatedReservation(date, themeId, timeId);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.fetchById(themeId);
        Reservation savedReservation = reservationRepository.save(new Reservation(member, theme, date, time));
        return new ReservationResponse(savedReservation);
    }

    public WaitingResponse createWaiting(ReservationCreate reservationInfo) {
        Reservation reservation = reservationRepository.findByDateAndThemeIdAndTimeId(
                reservationInfo.getDate(),
                reservationInfo.getThemeId(),
                reservationInfo.getTimeId()
        ).orElseThrow(ReservationNotFoundException::new);
        Member member = memberRepository.findByEmail(reservationInfo.getEmail())
                .orElseThrow(MemberNotFoundException::new);
        validateMineReservation(reservation, member);
        validateDuplicatedWaiting(reservation, member);

        Waiting waiting = waitingRepository.save(new Waiting(reservation, member, LocalDateTime.now()));

        return new WaitingResponse(waiting);
    }

    private void validateMineReservation(Reservation reservation, Member member) {
        if (reservation.getMember().getId().equals(member.getId())) {
            throw new CannotWaitingForMineException();
        }
    }

    private void validateDuplicatedWaiting(Reservation reservation, Member member) {
        if (waitingRepository.existsByReservationIdAndMemberEmail(reservation.getId(), member.getEmail())) {
            throw new WaitingConflictException();
        }
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException();
        }
        approveWaiting(id);
    }

    public void deleteWaiting(String email, long reservationId) {
        waitingRepository.deleteByReservationIdAndMemberEmail(reservationId, email);
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

    private void approveWaiting(long reservationId) {
        List<Waiting> waitings = waitingRepository.findByReservationId(reservationId);
        if (waitings.isEmpty()) {
            return;
        }

        Waiting primaryWaiting = waitings.stream()
                .min(Comparator.comparing(Waiting::getCreatedAt))
                .get();
        Reservation reservation = primaryWaiting.getReservation();
        reservation.assignWaitingMember(primaryWaiting.getMember());
        reservationRepository.save(reservation);

        waitingRepository.deleteById(primaryWaiting.getId());
    }
}
