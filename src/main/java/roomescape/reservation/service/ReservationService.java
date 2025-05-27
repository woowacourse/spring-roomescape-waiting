package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberId;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeId;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeId;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.dto.request.FilteringReservationRequest;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.BookedReservationTimeResponse;
import roomescape.reservation.dto.response.MyReservationsResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository,
            final WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationsResponse> getAllMyReservations(final LoginMember loginMember) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(new MemberId(loginMember.id()))
                .stream()
                .toList();
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findAllWaitingWithRankByMemberId(
                        new MemberId(loginMember.id()))
                .stream()
                .toList();
        return toMyReservationResponses(reservations, waitingWithRanks);
    }

    private List<MyReservationsResponse> toMyReservationResponses(
            final List<Reservation> reservations,
            final List<WaitingWithRank> waitingWithRanks
    ) {
        List<MyReservationsResponse> responses = new ArrayList<>();
        for (Reservation reservation : reservations) {
            responses.add(MyReservationsResponse.from(reservation));
        }
        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            responses.add(MyReservationsResponse.from(waitingWithRank));
        }
        return responses;
    }

    @Transactional
    public ReservationResponse create(final ReservationCreateRequest request) {
        if (isAlreadyBooked(request)) {
            throw new AlreadyInUseException("이미 예약이 존재합니다..");
        }
        if (hasAlreadyWaiting(request)) {
            throw new AlreadyInUseException("예약 대기가 존재해 예약을 생성할 수 없습니다.");
        }

        Reservation reservation = getReservation(request, request.loginMember());
        validateDateTime(LocalDateTime.now(), reservation.getDate(), reservation.getTime().getStartAt());

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    private boolean hasAlreadyWaiting(final ReservationCreateRequest request) {
        return waitingRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                new ReservationTimeId(request.timeId()),
                new ThemeId(request.themeId())
        );
    }

    public List<ReservationResponse> findReservationByFiltering(final FilteringReservationRequest request) {
        ThemeId themeId = new ThemeId(request.themeId());
        MemberId memberId = new MemberId(request.memberId());
        LocalDate dateFrom = request.dateFrom();
        LocalDate dateTo = request.dateTo();

        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationCreateRequest request) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                new ReservationTimeId(request.timeId()),
                new ThemeId(request.themeId())
        );
    }

    private Reservation getReservation(final ReservationCreateRequest request, final LoginMember loginMember) {
        ReservationTime reservationTime = getReservationTime(request);
        Theme theme = getTheme(request);
        MemberId memberId = new MemberId(loginMember.id());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("등록되지 않은 회원입니다."));
        return new Reservation(member, request.date(), reservationTime, theme);
    }

    private Theme getTheme(final ReservationCreateRequest request) {
        Long themeId = request.themeId();
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("theme not found id =" + themeId));
    }

    private ReservationTime getReservationTime(final ReservationCreateRequest request) {
        Long timeId = request.timeId();
        return reservationTimeRepository.findById(new ReservationTimeId(timeId))
                .orElseThrow(() -> new EntityNotFoundException("reservationsTime not found id =" + timeId));
    }

    private void validateDateTime(final LocalDateTime now, final LocalDate date, final LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if (now.isAfter(dateTime)) {
            throw new IllegalArgumentException("이미 지난 예약 시간입니다.");
        }
    }

    @Transactional
    public void delete(final Long id) {
        ReservationId reservationId = new ReservationId(id);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        reservationRepository.deleteById(reservationId);

        approveFirstWaiting(reservation);
    }

    private void approveFirstWaiting(final Reservation reservation) {
        Optional<Waiting> waiting = waitingRepository.findFirstByDateAndTimeAndThemeOrderByIdAsc(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        waiting.ifPresent(value -> {
            reservationRepository.save(new Reservation(
                    value.getMember(), value.getDate(), value.getTime(), value.getTheme()
            ));
            waitingRepository.delete(value);
        });
    }

    public List<BookedReservationTimeResponse> getSortedAvailableTimes(final LocalDate date, final Long themeId) {
        Map<ReservationTime, Boolean> allTimes = processAlreadyBookedTimesMap(date, themeId);

        return allTimes.keySet()
                .stream()
                .map(key -> bookedReservationTimeResponseOf(key, allTimes.get(key)))
                .sorted(Comparator.comparing(bookedReservationTimeResponse
                        -> bookedReservationTimeResponse.timeResponse().startAt()
                )).toList();
    }

    private Map<ReservationTime, Boolean> processAlreadyBookedTimesMap(final LocalDate date, final Long themeId) {
        Set<ReservationTime> alreadyBookedTimes = getAlreadyBookedTimes(date, themeId);

        return reservationTimeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Function.identity(), alreadyBookedTimes::contains));
    }

    private BookedReservationTimeResponse bookedReservationTimeResponseOf(
            final ReservationTime reservationTime,
            final boolean isAlreadyBooked
    ) {
        return new BookedReservationTimeResponse(ReservationTimeResponse.from(reservationTime), isAlreadyBooked);
    }

    private Set<ReservationTime> getAlreadyBookedTimes(final LocalDate date, final Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, new ThemeId(themeId))
                .stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
    }
}
