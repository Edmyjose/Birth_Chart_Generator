/*
 * Shredzone Commons - suncalc
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *   http://commons.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.ejs.birthchart.prototipe.util;

import static java.lang.Math.max;
import static java.lang.Math.toRadians;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.ejs.birthchart.prototipe.param.GenericParameter;
import com.ejs.birthchart.prototipe.param.LocationParameter;
import com.ejs.birthchart.prototipe.param.TimeParameter;

/**
 * A base implementation of {@link LocationParameter} and {@link TimeParameter}.
 * <p>
 * For internal use only.
 *
 * @param <T>
 *            Type of the final builder
 */
@SuppressWarnings("unchecked")
@RequiresApi(api = Build.VERSION_CODES.O)
public class BaseBuilder<T> implements GenericParameter<T>, LocationParameter<T>,
        TimeParameter<T>, Cloneable {

    private double lat = 0.0;
    private double lng = 0.0;
    private double height = 0.0;
    private ZonedDateTime dateTime = ZonedDateTime.now();

    @Override
    public T on(ZonedDateTime dateTime) {
        this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
        return (T) this;
    }

    @Override
    public T on(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime");
        return on(ZonedDateTime.of(dateTime, this.dateTime.getZone()));
    }

    @Override
    public T on(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return on(ZonedDateTime.of(date, LocalTime.MIDNIGHT, dateTime.getZone()));
    }

    @Override
    public T on(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return on(ZonedDateTime.ofInstant(instant, dateTime.getZone()));
    }

    @Override
    public T on(int year, int month, int date, int hour, int minute, int second) {
        return on(ZonedDateTime.of(year, month, date, hour, minute, second, 0, dateTime.getZone()));
    }

    @Override
    public T now() {
        return on(ZonedDateTime.now(dateTime.getZone()));
    }

    @Override
    public T plusDays(int days) {
        return on(dateTime.plusDays(days));
    }

    @Override
    public T midnight() {
        return on(dateTime.truncatedTo(ChronoUnit.DAYS));
    }

    @Override
    public T timezone(ZoneId tz) {
        Objects.requireNonNull(tz, "tz");
        on(dateTime.withZoneSameLocal(tz));
        return (T) this;
    }

    @Override
    public T latitude(double lat) {
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude out of range, -90.0 <= " + lat + " <= 90.0");
        }
        this.lat = lat;
        return (T) this;
    }

    @Override
    public T longitude(double lng) {
        if (lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Longitude out of range, -180.0 <= " + lng + " <= 180.0");
        }
        this.lng = lng;
        return (T) this;
    }

    @Override
    public T height(double h) {
        this.height = max(h, 0.0);
        return (T) this;
    }

    @Override
    public T sameTimeAs(TimeParameter<?> t) {
        if (! (t instanceof BaseBuilder)) {
            throw new IllegalArgumentException("Cannot read the TimeParameter");
        }
        this.dateTime = ((BaseBuilder<?>) t).dateTime;
        return (T) this;
    }

    @Override
    public T sameLocationAs(LocationParameter<?> l) {
        if (! (l instanceof BaseBuilder)) {
            throw new IllegalArgumentException("Cannot read the LocationParameter");
        }
        BaseBuilder<?> origin = (BaseBuilder<?>) l;
        this.lat = origin.lat;
        this.lng = origin.lng;
        this.height = origin.height;
        return (T) this;
    }

    @Override
    public T copy() {
        try {
            return (T) clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex); // Should never be thrown anyway
        }
    }

    /**
     * Returns the longitude.
     *
     * @return Longitude, in degrees.
     */
    public double getLongitude() {
        return lng;
    }

    /**
     * Returns the latitude.
     *
     * @return Latitude, in degrees.
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Returns the longitude.
     *
     * @return Longitude, in radians.
     */
    public double getLongitudeRad() {
        return toRadians(lng);
    }

    /**
     * Returns the latitude.
     *
     * @return Latitude, in radians.
     */
    public double getLatitudeRad() {
        return toRadians(lat);
    }

    /**
     * Returns the height, in meters above sea level.
     *
     * @return Height, meters above sea level
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the {@link JulianDate} to be used.
     *
     * @return {@link JulianDate}
     */
    public JulianDate getJulianDate() {
        return new JulianDate(dateTime);
    }

}
