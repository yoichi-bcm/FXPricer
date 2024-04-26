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
import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.calc.CalculationRunner;
import com.opengamma.strata.collect.io.ResourceLocator;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.data.MarketData;
import com.opengamma.strata.data.ObservableId;
import com.opengamma.strata.loader.csv.FixingSeriesCsvLoader;
import com.opengamma.strata.loader.csv.QuotesCsvLoader;
import com.opengamma.strata.loader.csv.RatesCalibrationCsvLoader;
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.curve.RatesCurveGroupDefinition;
import com.opengamma.strata.market.observable.QuoteId;
import com.opengamma.strata.pricer.curve.CalibrationMeasures;
import com.opengamma.strata.pricer.curve.RatesCurveCalibrator;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;

public class Engine {
  public static HolidayCalendar calendar = HolidayCalendars.of("AUSY");
  public static String DB_URL;
  public static String DB_PASSWORD;
  public static String DB_USERNAME;

  private static final String PATH_CONFIG = "Z:\\FX\\";  
  private static final String configPath = PATH_CONFIG+"fx_config.txt";
  public static final LocalDate dateToday = LocalDate.now();
  

  private static final CurveGroupName CURVE_GROUP_NAME = CurveGroupName.of("AUD-CURVE");
  private static final ResourceLocator GROUPS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "groups_fx.csv"));
  private static final ResourceLocator SETTINGS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "settings_fx.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_FX = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_fx.csv"));
//  private static final ResourceLocator GROUPS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/groups.csv"));
  private static final ResourceLocator QUOTES_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/quotes.csv"));
  private static final ResourceLocator FIXINGS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/aud-bbsw-fixings.csv"));
//  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates_hist.csv"));
//  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates.csv"));
//  private static final ResourceLocator SETTINGS_RESOURCE_PV = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/settings_pv.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_RBA = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_RBA.csv"));
  
public static void main(String[] args) throws FileNotFoundException, IOException, CsvValidationException {
    
    Map<String,String> configMap = getConfigMap();
    DB_URL = configMap.get("URL");
    DB_PASSWORD = configMap.get("Password");
    DB_USERNAME = configMap.get("Username");
    
    SaveBackTestFixings(dateToday);
    SaveQuotes(dateToday);
    
    GetCalibrations.generateCalibrations();
    try (CalculationRunner runnerxxx = CalculationRunner.ofMultiThreaded()) {
      LocalDate todayxxx = calendar.previous(LocalDate.now());
      ReferenceData refData = ReferenceData.standard();
      LocalDate VAL_DATE = LocalDate.now().minusDays(0);
      
      Map<CurveGroupName, RatesCurveGroupDefinition> defns =
          RatesCalibrationCsvLoader.load(GROUPS_FX_RESOURCE, SETTINGS_FX_RESOURCE, CALIBRATION_RESOURCE_RBA);
      ImmutableMap<ObservableId, LocalDateDoubleTimeSeries> fixings = FixingSeriesCsvLoader.load(FIXINGS_RESOURCE);
      CalibrationMeasures CALIBRATION_MEASURES = CalibrationMeasures.PAR_SPREAD;
      RatesCurveCalibrator CALIBRATOR = RatesCurveCalibrator.of(1e-3, 1e-3, 100, CALIBRATION_MEASURES);
      ImmutableMap<QuoteId, Double> MAP_MQ = QuotesCsvLoader.load(VAL_DATE, QUOTES_RESOURCE);
      MarketData marketData = MarketData.of(VAL_DATE, MAP_MQ, fixings);
      ImmutableRatesProvider multicurve = CALIBRATOR.calibrate(defns.get(CURVE_GROUP_NAME), marketData, refData);
      System.out.println();
      
      
//      Pricer.calculate(runnerxxx, todayxxx);
    }
  }
  
  
  private static Map<String, String> getConfigMap() {
    
    Map<String, String> configMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(configPath))) {
      String line;
      while ((line = br.readLine()) != null) {
          String[] parts = line.split("~");
          if (parts.length == 2) {
              String key = parts[0].trim();
              String value = parts[1].trim();
              configMap.put(key, value);
          }
      }
  } catch (IOException e) {
      // Handle file reading errors
      e.printStackTrace();
  }
    return configMap;
  }
  
  
  public static void SaveBackTestFixings(LocalDate calcdate){
    Connection conn = null;
    Statement sql= null; 
    String folderPath = PATH_CONFIG + "aud/quotes/";
    String fileName = "aud-bbsw-fixings.csv";
    Path filePath = Paths.get(folderPath, fileName);
    
    try
    {
      File file = new File(folderPath, fileName);
      if(file.exists()) {
        Files.delete(filePath);
      }      
      conn  = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
      sql = conn.createStatement();
      String query = 
          "SELECT * " +
          "FROM aud_fixings";
      //System.out.println(query);
      ResultSet rs = sql.executeQuery(query);   
      
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();      
      DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      
      try(FileWriter writer = new FileWriter(folderPath + fileName)) {
        for (int i = 1; i <= columnCount; i++) {
          String columnName = metaData.getColumnName(i);
          
          writer.append(columnName);
          if(i == (columnCount))
          {
            writer.append("\n");
          }
          else
          {
            writer.append(",");
          }
        }
       while (rs.next()) {         
         writer.append(rs.getString(1) + "," + LocalDate.parse(rs.getString(2)).format(formatterdate) + "," + rs.getString(3) + "\n");         
       } 
       writer.close();
      }catch (IOException e) {
        e.printStackTrace();
      }      
      conn.close();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println(e.getMessage());      
    }
  }
  
  public static void SaveQuotes(LocalDate calcdate) {
    Connection conn = null;
    Statement sql= null; 
    String folderPath = PATH_CONFIG + "aud/quotes/";
    String fileName = "quotes.csv";
    Path filePath = Paths.get(folderPath, fileName);
    try
    {
      File file = new File(folderPath, fileName);
      if(file.exists()) {
        Files.delete(filePath);
      }      
      conn  = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
      sql = conn.createStatement();
      String query = 
          "SELECT * " +
          "FROM aud_quotes" 
          ;
      //System.out.println(query);
      ResultSet rs = sql.executeQuery(query);   
      
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();      
      DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      
      try(FileWriter writer = new FileWriter(folderPath + fileName)) {
        for (int i = 1; i <= columnCount; i++) {
          String columnName = metaData.getColumnName(i);
          
          writer.append(columnName);
          if(i == (columnCount))
          {
            writer.append("\n");
          }
          else
          {
            writer.append(",");
          }
        }
       while (rs.next()) {         
         writer.append(LocalDate.parse(rs.getString(1)).format(formatterdate) + "," + rs.getString(2) + "," + rs.getString(3)+ "," + rs.getString(4)+ "," + rs.getDouble(5) + "\n");         
       } 
       writer.close();
      }catch (IOException e) {
        e.printStackTrace();
      }      
      conn.close();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println(e.getMessage());      
    }
  }
  
}
