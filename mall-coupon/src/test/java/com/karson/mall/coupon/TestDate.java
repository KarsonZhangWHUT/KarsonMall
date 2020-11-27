package com.karson.mall.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Karson
 */
public class TestDate {

    @Test
    public void demo(){
        LocalDate now = LocalDate.now();
        LocalDate plus2 = now.plusDays(2);
        LocalDateTime end = LocalDateTime.of(plus2, LocalTime.MAX);
        System.out.println(end);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);
    }
}
