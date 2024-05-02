package com.brahman.fx;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.currency.FxRate;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
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
import com.opengamma.strata.market.curve.CurveGroupName;
import com.opengamma.strata.market.curve.RatesCurveGroupDefinition;
import com.opengamma.strata.market.observable.QuoteId;
import com.opengamma.strata.market.param.CurrencyParameterSensitivities;
import com.opengamma.strata.measure.fx.FxSingleTradeCalculations;
import com.opengamma.strata.measure.fx.FxSwapTradeCalculations;
import com.opengamma.strata.measure.swap.SwapTradeCalculations;
import com.opengamma.strata.pricer.curve.CalibrationMeasures;
import com.opengamma.strata.pricer.curve.RatesCurveCalibrator;
import com.opengamma.strata.pricer.fx.DiscountingFxSingleTradePricer;
import com.opengamma.strata.pricer.fx.DiscountingFxSwapTradePricer;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.pricer.swap.DiscountingSwapTradePricer;
import com.opengamma.strata.product.AttributeType;
import com.opengamma.strata.product.TradeInfo;
import com.opengamma.strata.product.common.BuySell;
import com.opengamma.strata.product.fx.FxSingle;
import com.opengamma.strata.product.fx.FxSingleTrade;
import com.opengamma.strata.product.fx.FxSwap;
import com.opengamma.strata.product.fx.FxSwapTrade;
import com.opengamma.strata.product.fx.ResolvedFxSingleTrade;
import com.opengamma.strata.product.fx.ResolvedFxSwapTrade;
import com.opengamma.strata.product.swap.ResolvedSwapTrade;
import com.opengamma.strata.product.swap.SwapTrade;
import com.opengamma.strata.product.swap.type.FixedOvernightSwapConventions;

public class Pricer {
  private static final CurveGroupName CURVE_GROUP_NAME = CurveGroupName.of("AUD-CURVE");
  private static final String PATH_CONFIG = "Z:\\FX\\";  
  private static final ResourceLocator GROUPS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "groups_fx.csv"));
  private static final ResourceLocator SETTINGS_FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "settings_fx.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_FX = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_fx.csv"));
  private static final ResourceLocator GROUPS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/groups.csv"));
  private static final ResourceLocator QUOTES_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/quotes.csv"));
  private static final ResourceLocator FIXINGS_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/aud-bbsw-fixings.csv"));
//  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates_hist.csv"));
  private static final ResourceLocator FX_RESOURCE = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/quotes/fx-rates.csv"));
  private static final ResourceLocator SETTINGS_RESOURCE_PV = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/settings_pv.csv"));
  private static final ResourceLocator CALIBRATION_RESOURCE_RBA = ResourceLocator.ofFile(new File(PATH_CONFIG + "aud/curves/calibrations_RBA.csv"));
  
  public static void calculate(CalculationRunner runner, LocalDate calcdate) throws FileNotFoundException{
    ReferenceData refData = ReferenceData.standard();
    HolidayCalendar calendar = HolidayCalendars.of("AUSY");
    LocalDate VAL_DATE = calcdate;
    LocalDate SETTLE_DATE = calendar.next(calendar.next(VAL_DATE));
    LocalDate VAL_DATE_TM1 = calendar.previousOrSame(VAL_DATE.minusDays(1));
    //LocalDate effectivedate = calendar.nextOrSame(calcdate.plusDays(2));
    //LocalDate maturitydate = calendar.nextOrSame(effectivedate.plusMonths(3));
    
    DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    Map<CurveGroupName, RatesCurveGroupDefinition> defns = RatesCalibrationCsvLoader.load(GROUPS_FX_RESOURCE, SETTINGS_FX_RESOURCE, CALIBRATION_RESOURCE_RBA);    
    ImmutableMap<ObservableId, LocalDateDoubleTimeSeries> fixings = FixingSeriesCsvLoader.load(FIXINGS_RESOURCE);
    CalibrationMeasures CALIBRATION_MEASURES = CalibrationMeasures.PAR_SPREAD;   
    //RatesCurveCalibrator CALIBRATOR = RatesCurveCalibrator.of(1e-2, 1e-2, 100, CALIBRATION_MEASURES);
    RatesCurveCalibrator CALIBRATOR = RatesCurveCalibrator.standard();    
    ImmutableMap<QuoteId, Double> MAP_MQ = QuotesCsvLoader.load(VAL_DATE, QUOTES_RESOURCE); 
    ImmutableMap<FxRateId, FxRate> FXMAP_MQ = FxRatesCsvLoader.load(VAL_DATE, FX_RESOURCE);
    
    //MarketData marketData = MarketData.of(VAL_DATE, MAP_MQ, fixings);
    MarketData marketData = ImmutableMarketData.builder(VAL_DATE)
        .addValueMap(MAP_MQ)
        .addValueMap(FXMAP_MQ)
        .build();
    
    ImmutableRatesProvider multicurve = CALIBRATOR.calibrate(defns.get(CURVE_GROUP_NAME), marketData, refData); 
    
    double audusd = getquote(calcdate.format(formatterdate), "AUD-FX-0D");
    //double bab = getquote(calcdate.format(formatterdate), "IRZ3");
    
    System.out.println("AUD/USD: " + audusd);
    
    LocalDate effectivedate = SETTLE_DATE;//LocalDate.of(2024, 2, 20);//calendar.previousOrSame(calcdate.plusMonths(-3));
    LocalDate maturitydate = calendar.next(SETTLE_DATE.plusYears(1));//LocalDate.of(2025, 2, 20);//calcdate;
        
    pricefxswap(SETTLE_DATE, multicurve, audusd, effectivedate, maturitydate);
    
    
    
    System.out.println();
  }
  
  public static void pricefxswap(LocalDate calcdate, ImmutableRatesProvider multicurve, Double fxrate, LocalDate startdate, LocalDate enddate) {
    ReferenceData refData = ReferenceData.standard();
    DiscountingFxSwapTradePricer  PRICER_FXSWAP = DiscountingFxSwapTradePricer.DEFAULT;
    List<ResolvedFxSwapTrade> FX_TRADES = new ArrayList<ResolvedFxSwapTrade>();;
    FxSwapTradeCalculations fxswapTradeCalc = new FxSwapTradeCalculations(PRICER_FXSWAP);
    BusinessDayAdjustment bdayadj = BusinessDayAdjustment.NONE;
    
    double notional = 1000_000_000;
    
    
    FxSwap fxswap = FxSwap.ofForwardPoints(
        CurrencyAmount.of(Currency.AUD, notional), 
        FxRate.of(Currency.AUD, Currency.USD, fxrate), 
        0.0,
        startdate,
        enddate);
    
    FxSwapTrade newfxswaptrade = FxSwapTrade.builder()
        .product(fxswap)
        .info(TradeInfo.builder()
            .id(StandardId.of("ID", "1"))
            .addAttribute(AttributeType.DESCRIPTION, "AUD 10,000/USD @ 1.62 swap")
            .settlementDate(calcdate)
            .build())
        .build();
    ResolvedFxSwapTrade resolvedfxswap = newfxswaptrade.resolve(refData);    
    
    double parspread = fxswapTradeCalc.parSpread(resolvedfxswap, multicurve);    
    parspread = 0.0050;
    FxSwap fxswappar = FxSwap.ofForwardPoints(
        CurrencyAmount.of(Currency.AUD, notional), 
        FxRate.of(Currency.AUD, Currency.USD, fxrate), 
        parspread,
        startdate,
        enddate);
    
    FxSwapTrade newfxswaptradepar = FxSwapTrade.builder()
        .product(fxswappar)
        .info(TradeInfo.builder()
            .id(StandardId.of("ID", "1"))
            .addAttribute(AttributeType.DESCRIPTION, "")
            .settlementDate(calcdate)
            .build())
        .build();    
    ResolvedFxSwapTrade resolvedfxswappar = newfxswaptradepar.resolve(refData);  
    
    resolvedfxswappar.getProduct().getNearLeg();
    resolvedfxswappar.getProduct().getFarLeg();
    FX_TRADES.add(resolvedfxswap);
//    FX_TRADES.add(resolvedfxswap1);
//    FX_TRADES.add(resolvedfxswap2);
    
    
//    MultiCurrencyAmount tradeamount = fxswapTradeCalc.currencyExposure(resolvedfxswappar, multicurve);
//    MultiCurrencyAmount tradecash = fxswapTradeCalc.currentCash(resolvedfxswappar, multicurve);
    //CurrencyParameterSensitivities tradesensi = fxswapTradeCalc.pv01MarketQuoteBucketed(resolvedfxswappar, multicurve); 
    
    MultiCurrencyAmount tradepv = fxswapTradeCalc.presentValue(resolvedfxswappar, multicurve);
    
    //ExportSQL.create_fxrisk("audfx_liverisk",tradesensi, parspread, tradepv.convertedTo(Currency.AUD, multicurve).getAmount());
    
    
    System.out.println("FX SWAP Pxing");
    System.out.println("parspread: " + parspread*10000);
    System.out.println("tradepv: " + tradepv.convertedTo(Currency.AUD, multicurve));
    System.out.println();
  }
  
  
  public static double getquote(String formatdate, String pxticker) {
    Connection conn = null;
    Statement sql= null;
    double pxquote =0.0;
    try
    {
      conn  = DriverManager.getConnection("jdbc:postgresql://localhost:5432/brahman_aud", "postgres", "K0bayashi");      
      sql = conn.createStatement();
      
      String query = "SELECT value FROM aud_quotes WHERE \"valuation date\" = '" + formatdate + "' AND ticker = '" + pxticker + "'";
        
      ResultSet rs = sql.executeQuery(query);
      
      while (rs.next()) {
        pxquote= rs.getDouble(1);
        break;
      }
      conn.close();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println(e.getMessage());      
    }    
    return pxquote;
  }
  
  
  public static void pricefxfwd(LocalDate calcdate, ImmutableRatesProvider multicurve, Double fxrate, LocalDate fwddate) {
    ReferenceData refData = ReferenceData.standard(); 
    FxSingle newfx = FxSingle.of(
        CurrencyAmount.of(Currency.USD, 100000000), 
        FxRate.of(Currency.AUD, Currency.USD, fxrate), 
        fwddate);
    
    FxSingleTrade newfxtrade = FxSingleTrade.builder()
        .product(newfx)
        .info(TradeInfo.builder()
            .id(StandardId.of("example", "2"))
            .addAttribute(AttributeType.DESCRIPTION, "JPY 15,000/USD @ 149 fwd")
            .counterparty(StandardId.of("example", "BigBankB"))
            .settlementDate(calcdate)
            .build())
        .build();
    
    ResolvedFxSingleTrade resolvedfx = newfxtrade.resolve(refData);    
    
    DiscountingFxSingleTradePricer  PRICER_FX = DiscountingFxSingleTradePricer .DEFAULT;
    FxSingleTradeCalculations fxTradeCalc = new FxSingleTradeCalculations(PRICER_FX);
    FxRate fxparrate = fxTradeCalc.forwardFxRate(resolvedfx, multicurve);
    MultiCurrencyAmount tradeamount = fxTradeCalc.currencyExposure(resolvedfx, multicurve);
    MultiCurrencyAmount tradecash = fxTradeCalc.currentCash(resolvedfx, multicurve);
    CurrencyParameterSensitivities tradesensi = fxTradeCalc.pv01CalibratedBucketed(resolvedfx, multicurve);
    MultiCurrencyAmount tradepv = fxTradeCalc.presentValue(resolvedfx, multicurve);
    

    System.out.println();
    System.out.println("FX FWD Pxing: " + fwddate);
    System.out.println("parrate:" + fxparrate);
    System.out.println("swaprate:" + (fxparrate.fxRate(CurrencyPair.of(Currency.AUD, Currency.USD))-fxrate)*10000.0);
    System.out.println("tradepv: " + tradeamount);
    
    
  }

  public static ResolvedFxSwapTrade createfxswaptrade(double notional, LocalDate startdate, LocalDate enddate, double fxswapspread, double fxspot) {
    ReferenceData refData = ReferenceData.standard();
    HolidayCalendar calendar = HolidayCalendars.of("AUSY");
    DateTimeFormatter formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    //double audusd = getquote(startdate.format(formatterdate), "AUD-FX-0D");
    
    FxSwap fxswap = FxSwap.ofForwardPoints(
        CurrencyAmount.of(Currency.AUD, notional), 
        FxRate.of(Currency.AUD, Currency.USD, fxspot), 
        fxswapspread,
        startdate,
        enddate);
    
    FxSwapTrade newfxswaptrade = FxSwapTrade.builder()
        .product(fxswap)
        .info(TradeInfo.builder()
            .id(StandardId.of("example", "3"))
            .addAttribute(AttributeType.DESCRIPTION, "AUD 10,000/USD @ 1.62 swap")
            .counterparty(StandardId.of("example", "BankA"))
            .settlementDate(startdate)
            .build())
        .build();
    ResolvedFxSwapTrade resolvedfxswap = newfxswaptrade.resolve(refData);    
    return resolvedfxswap;
  }
  
  public static ResolvedSwapTrade createaudoisswaptrade(double notional, LocalDate startdate, LocalDate enddate, double swaprate) {
    ReferenceData refData = ReferenceData.standard();
    
    TradeInfo tradeInfo = TradeInfo.builder()
        .addAttribute(AttributeType.DESCRIPTION, "AUD_FIXED_TERM_AONIA_OIS")
        .build();
        
    SwapTrade newaudaonia = Conventions.AUD_FIXED_TERM_AONIA_OIS.toTrade(
        startdate, 
        startdate, 
        enddate, 
        BuySell.BUY, 
        notional, 
        swaprate);
    
    SwapTrade newswapwithInfo = newaudaonia.toBuilder()
        .info(tradeInfo)
        .build();    
    
    ResolvedSwapTrade resolvedaudaonia = newswapwithInfo.resolve(refData);
    return resolvedaudaonia;
  }
  
  public static ResolvedSwapTrade createusdoisswaptrade(double notional, LocalDate startdate, LocalDate enddate, double swaprate) {
    ReferenceData refData = ReferenceData.standard();
    
    TradeInfo tradeInfo = TradeInfo.builder()
        .addAttribute(AttributeType.DESCRIPTION, "USD_FIXED_1Y_SOFR_OIS")
        .build();
        
    SwapTrade newusdsofr = FixedOvernightSwapConventions.USD_FIXED_1Y_SOFR_OIS.toTrade(
        startdate, 
        startdate, 
        enddate, 
        BuySell.BUY, 
        notional, 
        swaprate);
    
    SwapTrade newswapwithInfo = newusdsofr.toBuilder()
        .info(tradeInfo)
        .build();    
    
    ResolvedSwapTrade resolvedusdaonia = newswapwithInfo.resolve(refData);
    return resolvedusdaonia;
  }
  
  public static double getRate(double notional,LocalDate startDate, LocalDate enddate, ImmutableRatesProvider multicurve, String id) {
    LocalDate startdate = Engine.VAL_DATE;
    ResolvedSwapTrade resolvedAud = createaudoisswaptrade(100, startdate, enddate, 0);
    ResolvedSwapTrade resolvedUsd = createusdoisswaptrade(100, startdate, enddate, 0);
    
    DiscountingSwapTradePricer PRICER_SWAP = DiscountingSwapTradePricer.DEFAULT;
    SwapTradeCalculations swapTradeCalc = new SwapTradeCalculations(PRICER_SWAP);
    double audParRate = swapTradeCalc.parRate(resolvedAud, multicurve);
    double usdParRate = swapTradeCalc.parRate(resolvedUsd, multicurve);

//    double pvAud = swapTradeCalc.presentValue(resolvedAud, multicurve).getAmount(Currency.AUD).getAmount() / Math.pow(1 + audParRate, (enddate.toEpochDay() - startdate.toEpochDay()) / 365.0);
//    double pvUsd = swapTradeCalc.presentValue(resolvedUsd, multicurve).getAmount(Currency.USD).getAmount() / Math.pow(1 + usdParRate, (enddate.toEpochDay() - startdate.toEpochDay()) / 365.0);
    
    ;
    double swaprate = ((1+ usdParRate)) / (1 + audParRate)* 0.65;
    
    System.out.println(id + ": " + swaprate);
    return swaprate;
  }
  
  public static double qwerty(LocalDate enddate, ImmutableRatesProvider multicurve) {
    LocalDate startdate = Engine.VAL_DATE;
    ResolvedSwapTrade resolvedAud = createaudoisswaptrade(100, startdate, enddate, 0);
    ResolvedSwapTrade resolvedUsd = createusdoisswaptrade(100, startdate, enddate, 0);

    DiscountingSwapTradePricer PRICER_SWAP = DiscountingSwapTradePricer.DEFAULT;
    SwapTradeCalculations swapTradeCalc = new SwapTradeCalculations(PRICER_SWAP);
    
    return 0;
  }
  
  
}
