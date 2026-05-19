package roomescape.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.exception.ConflictException;
import roomescape.exception.code.ConflictCode;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcTemplateReservationRepository implements ReservationRepository {

    private static final String SELECT_RESERVATION_JOIN =
            "SELECT r.id AS reservation_id, r.date, " +
                    "u.id AS user_id, u.uid AS user_uid, u.name AS user_name, " +
                    "t.id AS time_id, t.start_at, " +
                    "th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, " +
                    "th.thumbnail_url AS theme_thumbnail_url " +
                    "FROM reservation r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "JOIN reservation_time t ON r.time_id = t.id " +
                    "JOIN theme th ON r.theme_id = th.id ";

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        try {
            Reservation reservation = jdbcTemplate.queryForObject(
                    SELECT_RESERVATION_JOIN + "WHERE r.id = ?",
                    reservationRowMapper(),
                    id
            );
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> {
            User user = new User(
                    rs.getLong("user_id"),
                    rs.getString("user_uid"),
                    rs.getString("user_name"));
            ReservationTime reservationTime = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("start_at").toLocalTime());
            Theme theme = new Theme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail_url"));
            return new Reservation(
                    rs.getLong("reservation_id"),
                    user,
                    rs.getDate("date").toLocalDate(),
                    reservationTime,
                    theme);
        };
    }

    @Override
    public List<Reservation> findAllReservations() {
        return jdbcTemplate.query(SELECT_RESERVATION_JOIN, reservationRowMapper());
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(
                    conn -> {
                        PreparedStatement preparedStatement = conn.prepareStatement(
                                "INSERT INTO reservation(user_id, date, time_id, theme_id) " +
                                        "VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                        preparedStatement.setLong(1, reservation.userId());
                        preparedStatement.setDate(2, java.sql.Date.valueOf(reservation.date()));
                        preparedStatement.setLong(3, reservation.timeId());
                        preparedStatement.setLong(4, reservation.themeId());

                        return preparedStatement;
                    },
                    keyHolder);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ConflictCode.RESERVATION_DUPLICATED);
        }

        return new Reservation(
                Objects.requireNonNull(keyHolder.getKey()).longValue(),
                reservation.user(),
                reservation.date(),
                reservation.time(),
                reservation.theme());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public int relocateToCanceledReservation(Long id) {
        return jdbcTemplate.update(
                "INSERT INTO canceled_reservation (id, user_id, date, time_id, theme_id) " +
                        "SELECT id, user_id, date, time_id, theme_id FROM reservation WHERE id = ?",
                id);
    }

    @Override
    public List<Reservation> findReservationsByUserId(Long userId) {
        return jdbcTemplate.query(
                SELECT_RESERVATION_JOIN + "WHERE r.user_id = ?",
                reservationRowMapper(),
                userId
        );
    }

    @Override
    public int countReservationsOf(LocalDate date, long timeId, long themeId) {
        return Objects.requireNonNull(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) cnt FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class,
                date, timeId, themeId));
    }

    @Override
    public void updateReservation(Long id, LocalDate date, long timeId) {
        try {
            jdbcTemplate.update(
                    "UPDATE reservation SET date = ?, time_id = ? WHERE id = ?",
                    date, timeId, id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException();
        }
    }
}
