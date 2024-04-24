package com.brahman.fx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;

import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.calc.CalculationRunner;

public class Engine {
  public static HolidayCalendar calendar = HolidayCalendars.of("AUSY");
  public static void main(String[] args) throws FileNotFoundException, IOException {
    try (CalculationRunner runnerxxx = CalculationRunner.ofMultiThreaded()) {
      LocalDate todayxxx = calendar.previous(LocalDate.now());
      
    }
  }
}
