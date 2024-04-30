package com.brahman.fx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opencsv.exceptions.CsvValidationException;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.calc.CalculationRunner;
import com.opengamma.strata.collect.io.ResourceLocator;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.data.FxRateId;
import com.opengamma.strata.data.ImmutableMarketData;
import com.opengamma.strata.data.MarketData;
import com.opengamma.strata.data.ObservableId;
import com.opengamma.strata.loader.csv.FixingSeriesCsvLoader;
import com.opengamma.strata.loader.csv.FxRatesCsvLoader;
import com.opengamma.strata.loader.csv.QuotesCsvLoader;
import com.opengamma.strata.loader.csv.RatesCalibrationCsvLoader;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.RatesCurveGroupDefinition;
import com.opengamma.strata.market.observable.QuoteId;
import com.opengamma.strata.measure.swap.SwapTradeCalculations;
import com.opengamma.strata.pricer.curve.CalibrationMeasures;
import com.opengamma.strata.pricer.curve.RatesCurveCalibrator;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.pricer.swap.DiscountingSwapTradePricer;
import com.opengamma.strata.product.swap.ResolvedSwapTrade;

public class Engine {
  public static HolidayCalendar calendar = HolidayCalendars.of("AUSY");
  public static String DB_URL;
  public static String DB_PASSWORD;
  public static String DB_USERNAME;

  public static final String PATH_CONFIG = "Z:\\FX\\";  
  public static final String configPath = PATH_CONFIG+"fx_config.txt";
  public static final LocalDate dateToday = LocalDate.now();
  

  private static final CurveGroupName CURVE_GROUP_NAME = CurveGroupName.of("AUD-CURVE");
  private static final ResourceLocator GROUPS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "groups_fx.csv"));
  private static final ResourceLocator GROUPS_XCCY_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "groups_fx_2.csv"));
  private static final ResourceLocator SETTINGS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "settings_fx.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_FX = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_fx.csv"));
//  private static final ResourceLocator GROUPS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/groups.csv"));
  private static final ResourceLocator QUOTES_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/quotes.csv"));
  private static final ResourceLocator FX_RATES_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates-xccy-ois.csv"));
  private static final ResourceLocator FIXINGS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/aud-bbsw-fixings.csv"));
//  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates_hist.csv"));
//  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates.csv"));
//  private static final ResourceLocator SETTINGS_RESOURCE_PV = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/settings_pv.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_RBA = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_RBA.csv"));
  public static LocalDate VAL_DATE = LocalDate.now().minusDays(0);
public static void main(String[] args) throws FileNotFoundException, IOException, CsvValidationException {
    
    Map<String,String> configMap = GetFiles.getConfigMap();
    DB_URL = configMap.get("URL");
    DB_PASSWORD = configMap.get("Password");
    DB_USERNAME = configMap.get("Username");
    
    GetFiles.SaveBackTestFixings(dateToday);
    GetFiles.SaveQuotes(dateToday);
    
    GetFiles.generateCalibrations();
    
    ImmutableRatesProvider multicurve = getMulticurve(GROUPS_FX_RESOURCE, SETTINGS_FX_RESOURCE,
        QUOTES_RESOURCE, FX_RATES_RESOURCE, CALIBRATION_RESOURCE_RBA);

    ImmutableRatesProvider multicurve_xccy = getMulticurve(GROUPS_XCCY_FX_RESOURCE, SETTINGS_FX_RESOURCE,
        QUOTES_RESOURCE, FX_RATES_RESOURCE, CALIBRATION_RESOURCE_RBA);
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusDays(7));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusDays(7)), multicurve, "1w");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusDays(7));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusDays(7)), multicurve_xccy, "1w xccy");
      System.out.println();
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusDays(14));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusDays(14)), multicurve, "2w");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusDays(14));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusDays(14)), multicurve_xccy, "2w xccy");
      System.out.println();
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(1));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(1)), multicurve, "1m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(1));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(1)), multicurve_xccy, "1m xccy");
      System.out.println();
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(2));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(2)), multicurve, "2m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(2));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(2)), multicurve_xccy, "2m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(3));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(3)), multicurve, "3m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(3));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(3)), multicurve_xccy, "3m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(4));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(4)), multicurve, "4m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(4));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(4)), multicurve_xccy, "4m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(5));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(5)), multicurve, "5m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(5));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(5)), multicurve_xccy, "5m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(6));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(6)), multicurve, "6m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(6));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(6)), multicurve_xccy, "6m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(9));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(9)), multicurve, "9m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(9));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(9)), multicurve_xccy, "9m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(12));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(12)), multicurve, "12m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(12));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(12)), multicurve_xccy, "12m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(18));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(18)), multicurve, "18m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(18));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(18)), multicurve_xccy, "18m xccy");
      System.out.println();
      
      Pricer.pricefxfwd(VAL_DATE, multicurve, 0.65, VAL_DATE.plusMonths(24));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(24)), multicurve, "24m");
      Pricer.pricefxfwd(VAL_DATE, multicurve_xccy, 0.65, VAL_DATE.plusMonths(24));
      Pricer.getRate(100, calendar.nextOrSame(VAL_DATE.plusMonths(24)), multicurve_xccy, "24m xccy");
      System.out.println();

  }
  
  public static ImmutableRatesProvider getMulticurve(ResourceLocator groups, ResourceLocator settings, ResourceLocator quotes, ResourceLocator fxRate, ResourceLocator calibration) {
    try (CalculationRunner runnerxxx = CalculationRunner.ofMultiThreaded()) {
      ReferenceData refData = ReferenceData.standard();
      Map<CurveGroupName, RatesCurveGroupDefinition> defns =
          RatesCalibrationCsvLoader.load(groups, settings, calibration);
      CalibrationMeasures CALIBRATION_MEASURES = CalibrationMeasures.PAR_SPREAD;
      RatesCurveCalibrator CALIBRATOR = RatesCurveCalibrator.of(1e-3, 1e-3, 100, CALIBRATION_MEASURES);
      ImmutableMap<QuoteId, Double> MAP_MQ = QuotesCsvLoader.load(VAL_DATE, quotes);
      Map<FxRateId, FxRate> fxRates = FxRatesCsvLoader.load(VAL_DATE, fxRate);
      MarketData marketData = ImmutableMarketData.builder(VAL_DATE)
          .addValueMap(MAP_MQ)
          .addValueMap(fxRates)
          .build();
      
      ImmutableRatesProvider multicurve = CALIBRATOR.calibrate(defns.get(CURVE_GROUP_NAME), marketData, refData);
      return multicurve;
    }
  }
  
  
}
