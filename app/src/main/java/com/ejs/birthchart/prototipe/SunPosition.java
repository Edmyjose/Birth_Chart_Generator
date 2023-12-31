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
package com.ejs.birthchart.prototipe;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static com.ejs.birthchart.prototipe.util.ExtendedMath.equatorialToHorizontal;
import static com.ejs.birthchart.prototipe.util.ExtendedMath.refraction;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.ejs.birthchart.prototipe.param.Builder;
import com.ejs.birthchart.prototipe.param.GenericParameter;
import com.ejs.birthchart.prototipe.param.LocationParameter;
import com.ejs.birthchart.prototipe.param.TimeParameter;
import com.ejs.birthchart.prototipe.util.BaseBuilder;
import com.ejs.birthchart.prototipe.util.JulianDate;
import com.ejs.birthchart.prototipe.util.Sun;
import com.ejs.birthchart.prototipe.util.Vector;

/**
 * Calculates the position of the sun.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class SunPosition {

    private final double azimuth;
    private final double altitude;
    private final double trueAltitude;
    private final double distance;

    private SunPosition(double azimuth, double altitude, double trueAltitude, double distance) {
        this.azimuth = (toDegrees(azimuth) + 180.0) % 360.0;
        this.altitude = toDegrees(altitude);
        this.trueAltitude = toDegrees(trueAltitude);
        this.distance = distance;
    }

    /**
     * Starts the computation of {@link SunPosition}.
     *
     * @return {@link Parameters} to set.
     */
    public static Parameters compute() {
        return new SunPositionBuilder();
    }

    /**
     * Collects all parameters for {@link SunPosition}.
     */
    public interface Parameters extends
            GenericParameter<Parameters>,
            LocationParameter<Parameters>,
            TimeParameter<Parameters>,
            Builder<SunPosition> {
    }

    /**
     * Builder for {@link SunPosition}. Performs the computations based on the parameters,
     * and creates a {@link SunPosition} object that holds the result.
     */
    private static class SunPositionBuilder extends BaseBuilder<Parameters> implements Parameters {
        @Override
        public SunPosition execute() {
            JulianDate t = getJulianDate();

            double lw = toRadians(-getLongitude());
            double phi = toRadians(getLatitude());
            Vector c = Sun.position(t);
            double h = t.getGreenwichMeanSiderealTime() - lw - c.getPhi();
            Vector horizontal = equatorialToHorizontal(h, c.getTheta(), c.getR(), phi);
            double hRef = refraction(horizontal.getTheta());

            return new SunPosition(horizontal.getPhi(),
                            horizontal.getTheta() + hRef,
                            horizontal.getTheta(),
                            horizontal.getR());
        }
    }

    /**
     * The visible sun altitude above the horizon, in degrees.
     * <p>
     * {@code 0.0} means the sun's center is at the horizon, {@code 90.0} at the zenith
     * (straight over your head). Atmospheric refraction is taken into account.
     *
     * @see #getTrueAltitude()
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * The true sun altitude above the horizon, in degrees.
     * <p>
     * {@code 0.0} means the sun's center is at the horizon, {@code 90.0} at the zenith
     * (straight over your head).
     *
     * @see #getAltitude()
     */
    public double getTrueAltitude() {
        return trueAltitude;
    }

    /**
     * Sun azimuth, in degrees, north-based.
     * <p>
     * This is the direction along the horizon, measured from north to east. For example,
     * {@code 0.0} means north, {@code 135.0} means southeast, {@code 270.0} means west.
     */
    public double getAzimuth() {
        return azimuth;
    }

    /**
     * Sun's distance, in kilometers.
     */
    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SunPosition[azimuth=").append(azimuth);
        sb.append("°, altitude=").append(altitude);
        sb.append("°, true altitude=").append(trueAltitude);
        sb.append("°, distance=").append(distance).append(" km]");
        return sb.toString();
    }

}
