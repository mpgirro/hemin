package io.disposia.engine.mapper;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

public class OldDateMapper {

    public static OldDateMapper INSTANCE = new OldDateMapper();
    public static ZoneId ZONE = ZoneId.of("Europe/Vienna");

    public String asString(LocalDateTime localDateTime) {
        try {
            return (localDateTime == null ? null : localDateTime.toString());
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public Long asMilliseconds(LocalDateTime localDateTime) {
        try {
            return (localDateTime == null ? null : asZonedDateTime(localDateTime)).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(long milliseconds) {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZONE);
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(String localDateTime) {
        try {
            return (localDateTime == null ? null : LocalDateTime.parse(localDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(Timestamp sqlTimestamp) {
        try {
            return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public LocalDateTime asLocalDateTime(ZonedDateTime zonedDateTime) {
        try {
            return (zonedDateTime == null ? null : LocalDateTime.from(zonedDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public ZonedDateTime asZonedDateTime(String zonedDateTime){
        try {
            return (zonedDateTime == null ? null : ZonedDateTime.parse(zonedDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public Timestamp asTimestamp(LocalDateTime localDateTime){
        try {
            return (localDateTime == null ? null : Timestamp.valueOf(localDateTime));
        } catch (DateTimeParseException e) {
            throw new RuntimeException( e );
        }
    }

    public Date asDate(LocalDate source) {
        return Optional.ofNullable(source)
            .map(d -> Date.from(d.atStartOfDay(ZONE).toInstant()))
            .orElse(null);

        /* TODO
        return source == null ? null : Date.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
        */
    }

    public Date asDate(ZonedDateTime source) {
        return Optional.ofNullable(source)
            .map(d ->Date.from(d.toInstant()))
            .orElse(null);

        /* TODO
        return source == null ? null : Date.from(source.toInstant());
        */
    }

    public Date asDate(LocalDateTime source) {
        return source == null ? null : Date.from(source.atZone(ZONE).toInstant());
    }

    public LocalDate asLocaleDate(Date source) {
        return source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), ZONE).toLocalDate();
    }

    public ZonedDateTime asZonedDateTime(Date source) {
        return source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), ZONE);
    }

    public LocalDateTime asLocalDateTime(Date source) {
        return source == null ? null : LocalDateTime.ofInstant(source.toInstant(), ZONE);
    }

    public java.sql.Date asSqlDate(LocalDate date) {
        return date == null ? null : java.sql.Date.valueOf(date);
    }

    public LocalDate asLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    public ZonedDateTime asZonedDateTime(LocalDateTime localDateTime) {
        return ZonedDateTime.ofInstant(localDateTime, ZoneOffset.UTC, ZONE);
    }

    public LocalDate asLocalDate(java.util.Date source) {
        return source == null ? null : ZonedDateTime.ofInstant(source.toInstant(), ZONE).toLocalDate();
    }

}
