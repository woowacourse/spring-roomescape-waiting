package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.DUPLICATE_WAITING_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.LoginMember;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Reservations;
import roomescape.domain.Waiting;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationDetailResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse save(LoginMember loginMember,
                                    ReservationRequest reservationRequest) {

        Reservation reservation = getReservation(loginMember.getId(), reservationRequest, ReservationStatus.BOOKED);

        Reservations reservations = new Reservations(reservationRepository.findAll());
        if (reservations.hasSameReservation(reservation)) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public ReservationResponse saveWaiting(LoginMember loginMember,
                                           ReservationRequest reservationRequest) {

        Reservation reservation = getReservation(loginMember.getId(), reservationRequest, ReservationStatus.WAITING);
        Reservations reservations = new Reservations(reservationRepository.findAllByMemberId(loginMember.getId()));
        if (reservations.hasSameReservation(reservation)) {
            throw new RoomescapeException(DUPLICATE_WAITING_RESERVATION);
        }
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public ReservationResponse saveByAdmin(AdminReservationRequest reservationRequest) {

        Reservation beforeSaveReservation = getReservation(reservationRequest.memberId(),
                new ReservationRequest(
                        reservationRequest.date(),
                        reservationRequest.timeId(),
                        reservationRequest.themeId()),
                ReservationStatus.BOOKED);

        Reservations reservations = new Reservations(reservationRepository.findAll());
        if (reservations.hasSameReservation(beforeSaveReservation)) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }

        return ReservationResponse.from(reservationRepository.save(beforeSaveReservation));
    }

    private Reservation getReservation(long memberId, ReservationRequest reservationRequest, ReservationStatus reservationStatus) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member requestedMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        Reservation reservation = reservationRequest.toReservation(
                requestedMember,
                requestedTime,
                requestedTheme,
                reservationStatus);

        if (reservation.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
        return reservation;
    }

    public List<ReservationResponse> findAll() {
        Reservations reservations = new Reservations(reservationRepository.findAll());
        return reservations.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> searchReservation(Long themeId, Long memberId, LocalDate dateFrom,
                                                       LocalDate dateTo) {
        Reservations reservations = new Reservations(reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo));
        return reservations.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationDetailResponse> findAllByMemberId(long memberId) {
        Reservations reservations = new Reservations(reservationRepository.findAllByMemberId(memberId));
        List<Waiting> waitings = reservations.waiting().stream()
                .map(reservation -> new Waiting(reservation,
                        reservationRepository.findAndCountWaitingNumber(
                                reservation.getDate(),
                                reservation.getReservationTime(),
                                reservation.getTheme(),
                                reservation.getCreatedAt())))
                .toList();
        return ReservationDetailResponse.of(reservations, waitings);
    }

    @Transactional
    public void deleteById(LoginMember loginMember, long id) {
        reservationRepository.deleteByMemberIdAndId(loginMember.getId(), id);
    }
}
