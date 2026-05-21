package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.exception.BadRequestException;
import roomescape.exception.code.BadRequestCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final ReservationTime TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, 1L, "우주 정거장", "설명", "https://example.com/1.jpg");
    private static final LocalDate DATE = LocalDate.of(2026, 5, 10);
    private static final User BROWN = new User(2L, "user1", "브라운", Role.USER);
    private static final User JOY = new User(3L, "user2", "조이", Role.USER);

    static Stream<Arguments> invalidDateTimes() {
        LocalTime startAt = TIME.startAt();
        return Stream.of(
                Arguments.of(LocalDateTime.of(DATE, startAt)),
                Arguments.of(LocalDateTime.of(DATE, startAt.minusMinutes(1)))
        );
    }

    @Test
    void 사용자가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(null, null, DATE, TIME, THEME))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(BadRequestCode.INVALID_RESERVATION_USER.getMessage());
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(null, BROWN, null, TIME, THEME))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(BadRequestCode.INVALID_RESERVATION_DATE.getMessage());
    }

    @Test
    void 시간이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(null, BROWN, DATE, null, THEME))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(BadRequestCode.INVALID_RESERVATION_TIME.getMessage());
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Reservation(null, BROWN, DATE, TIME, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(BadRequestCode.INVALID_RESERVATION_THEME.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidDateTimes")
    void 예약시간이_받은_datetime과_같거나_이전이면_false(LocalDateTime dateTime) {
        Theme theme = new Theme(1L, 1L, "theme1", "desc1", "https://example.com/1.jpg");
        Reservation reservation = new Reservation(null, BROWN, DATE, TIME, theme);
        assertThat(reservation.isDateTimeBefore(dateTime)).isFalse();
    }

    @Test
    void 예약시간이_받은_datetime과_이후면_true() {
        Theme theme = new Theme(1L, 1L, "theme1", "desc1", "https://example.com/1.jpg");
        Reservation reservation = new Reservation(null, BROWN, DATE, TIME, theme);
        LocalDateTime dateTime = LocalDateTime.of(DATE, TIME.startAt().plusMinutes(1));
        assertThat(reservation.isDateTimeBefore(dateTime)).isTrue();
    }

    @Test
    void 예약_날짜가_받은_날짜보다_이전이면_true() {
        Reservation reservation = new Reservation(null, BROWN, DATE, TIME, THEME);

        assertThat(reservation.isDateBefore(DATE.plusDays(1))).isTrue();
    }

    @Test
    void 예약_날짜가_받은_날짜와_같으면_false() {
        Reservation reservation = new Reservation(null, BROWN, DATE, TIME, THEME);

        assertThat(reservation.isDateBefore(DATE)).isFalse();
    }

    @Test
    void 예약_날짜가_받은_날짜보다_이후면_false() {
        Reservation reservation = new Reservation(null, BROWN, DATE, TIME, THEME);

        assertThat(reservation.isDateBefore(DATE.minusDays(1))).isFalse();
    }


    @Test
    void id가_같으면_같은_예약으로_본다() {
        Reservation a = new Reservation(1L, BROWN, DATE, TIME, THEME);
        Reservation b = new Reservation(1L, JOY, DATE.plusDays(1), TIME, THEME);

        assertThat(a)
                .isEqualTo(b)
                .hasSameHashCodeAs(b);
    }

    @Test
    void id가_다르면_다른_예약으로_본다() {
        Reservation a = new Reservation(1L, BROWN, DATE, TIME, THEME);
        Reservation b = new Reservation(2L, BROWN, DATE, TIME, THEME);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void 같은_사용자가_예약자면_isOwnedBy는_true() {
        Reservation reservation = new Reservation(1L, BROWN, DATE, TIME, THEME);

        assertThat(reservation.isOwnedBy(new User(2L, "any", "any", Role.USER))).isTrue();
    }

    @Test
    void 다른_사용자가_예약자면_isOwnedBy는_false() {
        Reservation reservation = new Reservation(1L, BROWN, DATE, TIME, THEME);

        assertThat(reservation.isOwnedBy(JOY)).isFalse();
    }
}
