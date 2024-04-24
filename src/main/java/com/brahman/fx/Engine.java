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

import com.opengamma.strata.basics.date.HolidayCalendar;
import com.opengamma.strata.basics.date.HolidayCalendars;
import com.opengamma.strata.calc.CalculationRunner;

public class Engine {
  public static HolidayCalendar calendar = HolidayCalendars.of("AUSY");
  public static String DB_URL;
  public static String DB_PASSWORD;
  public static String DB_USERNAME;

  private static final String PATH_CONFIG = "Z:\\FX\\";  
  private static final String configPath = PATH_CONFIG+"java_fx_config.txt";
  public static final LocalDate dateToday = LocalDate.now();
  
  private static Map<String, String> getConfigMap() {
    
    Map<String, String> configMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(configPath))) {
      String line;
      while ((line = br.readLine()) != null) {
          // Split each line into key and value based on ":"
          String[] parts = line.split("~");
          if (parts.length == 2) {
              String key = parts[0].trim();
              String value = parts[1].trim();
              // Add key-value pair to the map
              configMap.put(key, value);
          }
      }
  } catch (IOException e) {
      // Handle file reading errors
      e.printStackTrace();
  }
    return configMap;
  }
  
  public static void main(String[] args) throws FileNotFoundException, IOException {
    
    Map<String,String> configMap = getConfigMap();
    DB_URL = configMap.get("URL");
    DB_PASSWORD = configMap.get("Password");
    DB_USERNAME = configMap.get("Username");
    
    SaveBackTestFixings(dateToday);
    SaveQuotes(dateToday);
    
    try (CalculationRunner runnerxxx = CalculationRunner.ofMultiThreaded()) {
      LocalDate todayxxx = calendar.previous(LocalDate.now());
      Pricer.calculate(runnerxxx, todayxxx);
    }
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
          "FROM aud_fixings " +   
          "WHERE date < '" + calcdate + "'";
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
          "FROM aud_quotes " +   
          "WHERE \"valuation date\" < '" + calcdate + "'";
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
