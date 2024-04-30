package com.brahman.fx;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;


public class GetCalibrations {
  public static void generateCalibrations() throws CsvValidationException {
    List<LocalDate> RBADates = new ArrayList<>();
    RBADates.add(LocalDate.of(2024, 5, 6));
    RBADates.add(LocalDate.of(2024, 6, 17));
    RBADates.add(LocalDate.of(2024, 8, 5));
    RBADates.add(LocalDate.of(2024, 9, 23));
    RBADates.add(LocalDate.of(2024, 11, 4));
    RBADates.add(LocalDate.of(2024, 12, 9));
    int[] daysToMeet = new int[RBADates.size()];
    for(int i = 0; i < RBADates.size(); i++) {
      daysToMeet[i] = RBADates.get(i).getDayOfYear() - LocalDate.now().getDayOfYear();
    }
    String[] columns = new String[] {"Curve Name", "Label", "Symbology", "Ticker", "Field Name", "Type", "Convention", "Time","Date"};
    String[] row1 = new String[] {"AUD-Disc", "OIS-1D", "OG-Ticker", "AUD-AONIA", "MarketValue", "OIS", "AUD-FIXED-TERM-AONIA-OIS", "1D"};
    String RBACalibrations = "Z:\\FX\\calibrations_RBA.csv";
    try (CSVWriter writer = new CSVWriter(new FileWriter(RBACalibrations))) {
      writer.writeNext(columns);
      writer.writeNext(row1);
      int counter = 1;
      for (int i = 0; i < RBADates.size(); i++) {
        if(daysToMeet[i] > 0 ) {
          String[] row = new String[] {"AUD-Disc", "RBA-"+counter+"M", "OG-Ticker", "AUD-RBA-"+counter+"M", "MarketValue", "OIS", "AUD-FIXED-TERM-AONIA-OIS", daysToMeet[i]+"D", RBADates.get(i).toString()};
          writer.writeNext(row);
          counter = counter+1;
        }
      }
  } catch (IOException e) {
      e.printStackTrace();
  }
    
  }

  
}
