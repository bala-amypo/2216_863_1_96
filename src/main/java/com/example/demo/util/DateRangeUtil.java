package com.example.demo.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeUtil {

    private DateRangeUtil() {
        }

            public static boolean isValidRange(LocalDate start, LocalDate end) {
                    if (start == null || end == null) {
                                return false;
                                        }
                                                if (start.isAfter(end)) {
                                                            return false;
                                                                    }
                                                                            if (start.isAfter(LocalDate.now())) {
                                                                                        return false;
                                                                                                }
                                                                                                        return true;
                                                                                                            }

                                                                                                                public static long daysBetween(LocalDate start, LocalDate end) {
                                                                                                                        if (!isValidRange(start, end)) {
                                                                                                                                    throw new IllegalArgumentException("Start date or end date invalid or future");
                                                                                                                                            }
                                                                                                                                                    return ChronoUnit.DAYS.between(start, end) + 1;
                                                                                                                                                        }
                                                                                                                                                        }
                                                                                                                                                        