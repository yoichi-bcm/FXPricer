package com.brahman.fx;

import java.io.BufferedReader;
import java.io.File;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;


public class GetFiles {
  public static void generateCalibrations() throws CsvValidationException {
    List<LocalDate> RBADates = new ArrayList<>();
    
    RBADates = getRBADates(2024,2026);
    
    List<Double> spreads = new ArrayList<>();
    spreads.add(-0.0001);
    spreads.add(-0.0001);
    spreads.add(-0.00025);
    spreads.add(-0.00025);
    spreads.add(-0.00135);
    spreads.add(-0.00135);
    spreads.add(-0.0001);
    spreads.add(-0.0001);
    spreads.add(-0.00025);
    spreads.add(-0.00025);
    spreads.add(-0.00135);
    spreads.add(-0.00135);
    
    
    int[] daysToMeet = new int[RBADates.size()];
    for(int i = 0; i < RBADates.size(); i++) {
      daysToMeet[i] = (int) (RBADates.get(i).toEpochDay() - Engine.VAL_DATE.toEpochDay());
    }
    String[] columns = new String[] {"Curve Name", "Label", "Symbology", "Ticker", "Field Name", "Type", "Convention", "Time","Date","Spread"};
    String[] row1 = new String[] {"AUD-Disc", "OIS-1D", "OG-Ticker", "AUD-AONIA", "MarketValue", "OIS", "AUD-FIXED-TERM-AONIA-OIS", "1D"};
    String RBACalibrations = "Z:\\FX\\aud\\curves\\calibrations_RBA.csv";
    String defaultFxFwdCalibrations = "Z:\\FX\\aud\\curves\\calibrations_fxfwd.csv";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    try (CSVWriter writer = new CSVWriter(new FileWriter(RBACalibrations))) {
      writer.writeNext(columns);
      writer.writeNext(row1);
      int counter = 1;
      for (int i = 0; i < RBADates.size(); i++) {
        if(daysToMeet[i] > 0 && i+1 < 15) {
          String[] row = new String[] {"AUD-Disc", "RBA-"+counter+"M", "OG-Ticker",
              "AUD-RBA-"+counter+"M", "MarketValue", "OIS", "AUD-FIXED-TERM-AONIA-OIS",
              daysToMeet[i]+"D", LocalDate.parse(RBADates.get(i+1).toString(),formatter).toString(), spreads.get(counter-1)+""};
          writer.writeNext(row);
          counter = counter+1;
        }
      }
      
      try (CSVReader reader = new CSVReader(new FileReader(defaultFxFwdCalibrations))) {
        String[] nextLine;
        counter = 0;
        while ((nextLine = reader.readNext()) != null) {
          if (!nextLine[0].startsWith("#")) {
            if (counter > 0) {
              writer.writeNext(nextLine);
            }
            else {
              writer.writeNext(new String[]{""});
            }
            counter = counter+1;
          }
        }
    }
      
  } catch (IOException e) {
      e.printStackTrace();
  }
    
  }
  
  public static List<LocalDate> getRBADates(int startYear, int endYear) {
    List<LocalDate> mondays = new ArrayList<>();
    for (int year = startYear; year <= endYear; year++) {
        mondays.add(LocalDate.of(year, 2, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        mondays.add(LocalDate.of(year, 3, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).plusWeeks(2));
        mondays.add(LocalDate.of(year, 5, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        mondays.add(LocalDate.of(year, 6, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).plusWeeks(2));
        mondays.add(LocalDate.of(year, 8, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        mondays.add(LocalDate.of(year, 9, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).plusWeeks(2));
        mondays.add(LocalDate.of(year, 11, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)));
        mondays.add(LocalDate.of(year, 12, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).plusWeeks(1));
    }
    return mondays;
}
  
public static Map<String, String> getConfigMap() {
    
    Map<String, String> configMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(Engine.configPath))) {
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


  public static double getFxRates(LocalDate VAL_DATE) {
    Connection conn = null;
    Statement sql= null;
    DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    try {
      conn  = DriverManager.getConnection(Engine.DB_URL, Engine.DB_USERNAME, Engine.DB_PASSWORD);
      sql = conn.createStatement();
      String query = "select * from aud_quotes where ticker = 'AUD-FX-0D' and \"valuation date\" = '"+LocalDate.parse(VAL_DATE.toString()).format(formatterdate)+"'";
      ResultSet rs = sql.executeQuery(query);
      rs.next();
      double fx = rs.getDouble("value");
      try(CSVWriter writer = new CSVWriter(new FileWriter(Engine.PATH_CONFIG + "aud/quotes/fx-rates-xccy-ois.csv"))) {
        writer.writeNext(new String[] {"Valuation Date", "Currency Pair","Value"});
        writer.writeNext(new String[] {LocalDate.parse(VAL_DATE.toString()).format(formatterdate), "AUD/USD", (fx+"")});
        return fx;
      }catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return 0;
  }
  
  public static void SaveBackTestFixings(LocalDate calcdate){
    Connection conn = null;
    Statement sql= null; 
    String folderPath = Engine.PATH_CONFIG + "aud/quotes/";
    String fileName = "aud-bbsw-fixings.csv";
    Path filePath = Paths.get(folderPath, fileName);
    
    try
    {
      File file = new File(folderPath, fileName);
      if(file.exists()) {
        Files.delete(filePath);
      }      
      conn  = DriverManager.getConnection(Engine.DB_URL, Engine.DB_USERNAME, Engine.DB_PASSWORD);
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
    String folderPath = Engine.PATH_CONFIG + "aud/quotes/";
    String fileName = "quotes.csv";
    Path filePath = Paths.get(folderPath, fileName);
    try
    {
      File file = new File(folderPath, fileName);
      if(file.exists()) {
        Files.delete(filePath);
      }      
      conn  = DriverManager.getConnection(Engine.DB_URL, Engine.DB_USERNAME, Engine.DB_PASSWORD);
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
