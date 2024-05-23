package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.CANCELED;
import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;
import static roomescape.domain.reservation.ReservationStatus.REJECTED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.Comparator;
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
import roomescape.exception.reservation.ReservationDuplicatedException;
import roomescape.exception.reservation.ReservationNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.repository.dto.WaitingReservationResponse;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;
import roomescape.service.helper.QueryGenerator;

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

    @Transactional(readOnly = true)
    public List<ReservationResponse> searchConfirmedReservations(ReservationSearchParams request) {
        Specification<Reservation> specification = QueryGenerator.getSearchSpecification(request);
        return reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WaitingReservationResponse> findAllWaitingReservations() {
        return reservationRepository.findReservationByStatus(WAITING);
    }

    @Transactional(readOnly = true)
    public List<ReservationRankResponse> findMyReservations(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        List<ReservationRankResponse> myReservations = reservationRepository.findReservationRankByMember(member);
        myReservations.sort(Comparator
                .comparing(ReservationRankResponse::getDate)
                .thenComparing(ReservationRankResponse::getTime));
        return myReservations;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationCreate createInfo) {
        LocalDate date = createInfo.getDate();
        Member member = memberRepository.findByEmail(createInfo.getEmail()).orElseThrow(MemberNotFoundException::new);
        Theme theme = themeRepository.fetchById(createInfo.getThemeId());
        ReservationTime time = reservationTimeRepository.fetchById(createInfo.getTimeId());

        validateDuplicatedReservation(member, theme, date, time);

        Reservation reservation = generateReservation(member, theme, date, time);
        reservationRepository.save(reservation);
        return new ReservationResponse(reservation);
    }

    @Transactional
    public void memberCancelWaitingReservation(String email, long id) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        Reservation reservation = reservationRepository.findByIdAndMemberAndStatus(id, member, WAITING)
                .orElseThrow(ReservationNotFoundException::new);
        reservation.updateStatus(CANCELED);
    }

    @Transactional
    public void adminRejectConfirmedReservation(long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservation.updateStatus(REJECTED);
        Optional<Reservation> first = reservationRepository.findFirstByThemeAndDateAndTimeAndStatus(
                reservation.getTheme(),
                reservation.getDate(),
                reservation.getTime(),
                WAITING
        );
        first.ifPresent(value -> value.updateStatus(CONFIRMED));
    }

    @Transactional
    public void adminRejectWaitingReservation(long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(ReservationNotFoundException::new);
        reservation.updateStatus(REJECTED);
    }

    private void validateDuplicatedReservation(Member member, Theme theme, LocalDate date, ReservationTime time) {
        if (reservationRepository.existsByMemberAndThemeAndDateAndTime(member, theme, date, time)) {
            throw new ReservationDuplicatedException();
        }
    }

    private Reservation generateReservation(Member member, Theme theme, LocalDate date, ReservationTime time) {
        if (reservationRepository.existsByThemeAndDateAndTime(theme, date, time)) {
            return new Reservation(member, theme, date, time, WAITING);
        }
        return new Reservation(member, theme, date, time, CONFIRMED);
    }
}
