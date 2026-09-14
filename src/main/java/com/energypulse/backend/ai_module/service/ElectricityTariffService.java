package com.energypulse.backend.ai_module.service;

import org.springframework.stereotype.Service;

@Service
public class ElectricityTariffService {

    /*
     * Configure these rates according to the electricity
     * tariff applicable to the period/location used in
     * your coursework.
     *
     * This implementation uses a progressive slab calculation.
     */

    private static final double SLAB_1_LIMIT = 30;
    private static final double SLAB_2_LIMIT = 60;
    private static final double SLAB_3_LIMIT = 90;
    private static final double SLAB_4_LIMIT = 180;

    private static final double RATE_1 = 10.0;
    private static final double RATE_2 = 15.0;
    private static final double RATE_3 = 20.0;
    private static final double RATE_4 = 30.0;
    private static final double RATE_5 = 40.0;

    public double calculateCost(
            double kwh
    ) {

        if (kwh <= 0) {
            return 0;
        }

        double remaining = kwh;

        double cost = 0;

        double first =
                Math.min(
                        remaining,
                        SLAB_1_LIMIT
                );

        cost += first * RATE_1;

        remaining -= first;

        if (remaining <= 0) {
            return round(cost);
        }

        double second =
                Math.min(
                        remaining,
                        SLAB_2_LIMIT - SLAB_1_LIMIT
                );

        cost += second * RATE_2;

        remaining -= second;

        if (remaining <= 0) {
            return round(cost);
        }

        double third =
                Math.min(
                        remaining,
                        SLAB_3_LIMIT - SLAB_2_LIMIT
                );

        cost += third * RATE_3;

        remaining -= third;

        if (remaining <= 0) {
            return round(cost);
        }

        double fourth =
                Math.min(
                        remaining,
                        SLAB_4_LIMIT - SLAB_3_LIMIT
                );

        cost += fourth * RATE_4;

        remaining -= fourth;

        if (remaining > 0) {

            cost +=
                    remaining * RATE_5;
        }

        return round(cost);
    }

    /*
     * Reverse calculation.
     *
     * Because electricity billing is progressive,
     * we calculate the kWh slab by slab rather than
     * simply doing amount / one fixed rate.
     */
    public double calculateKwhFromAmount(
            double amount
    ) {

        if (amount <= 0) {
            return 0;
        }

        double remainingAmount =
                amount;

        double kwh = 0;

        double slab1Cost =
                SLAB_1_LIMIT * RATE_1;

        if (remainingAmount <= slab1Cost) {

            return round(
                    remainingAmount
                            / RATE_1
            );
        }

        kwh += SLAB_1_LIMIT;

        remainingAmount -=
                slab1Cost;

        double slab2Units =
                SLAB_2_LIMIT
                        - SLAB_1_LIMIT;

        double slab2Cost =
                slab2Units * RATE_2;

        if (remainingAmount <= slab2Cost) {

            return round(
                    kwh
                            + remainingAmount
                            / RATE_2
            );
        }

        kwh += slab2Units;

        remainingAmount -=
                slab2Cost;

        double slab3Units =
                SLAB_3_LIMIT
                        - SLAB_2_LIMIT;

        double slab3Cost =
                slab3Units * RATE_3;

        if (remainingAmount <= slab3Cost) {

            return round(
                    kwh
                            + remainingAmount
                            / RATE_3
            );
        }

        kwh += slab3Units;

        remainingAmount -=
                slab3Cost;

        double slab4Units =
                SLAB_4_LIMIT
                        - SLAB_3_LIMIT;

        double slab4Cost =
                slab4Units * RATE_4;

        if (remainingAmount <= slab4Cost) {

            return round(
                    kwh
                            + remainingAmount
                            / RATE_4
            );
        }

        kwh += slab4Units;

        remainingAmount -=
                slab4Cost;

        kwh +=
                remainingAmount
                        / RATE_5;

        return round(kwh);
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100
        ) / 100.0;
    }
}