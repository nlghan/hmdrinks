package com.hmdrinks.Service.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class ValueComparator implements Comparator<Long> {
    private final Map<Long, Double> base;

    public ValueComparator(Map<Long, Double> base) {
        this.base = base;
    }

    public int compare(Long a, Long b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
}
