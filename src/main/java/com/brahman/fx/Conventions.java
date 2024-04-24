package com.brahman.fx;

import static com.opengamma.strata.basics.currency.Currency.AUD;
import static com.opengamma.strata.basics.currency.Currency.USD;
import static com.opengamma.strata.basics.date.BusinessDayConventions.MODIFIED_FOLLOWING;
import static com.opengamma.strata.basics.date.DateSequences.QUARTERLY_10TH;
import static com.opengamma.strata.basics.date.DayCounts.ACT_365F;
import static com.opengamma.strata.basics.date.DayCounts.ACT_365_ACTUAL;
import static com.opengamma.strata.basics.date.HolidayCalendarIds.AUSY;
import static com.opengamma.strata.basics.index.OvernightIndices.AUD_AONIA;
import static com.opengamma.strata.basics.schedule.Frequency.P12M;
import static com.opengamma.strata.basics.schedule.Frequency.P3M;
import static com.opengamma.strata.basics.schedule.Frequency.P6M;
import static com.opengamma.strata.basics.schedule.Frequency.TERM;
import static com.opengamma.strata.product.swap.OvernightAccrualMethod.COMPOUNDED;

import com.opengamma.strata.basics.currency.CurrencyPair;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.DayCounts;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.HolidayCalendarId;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.index.IborIndices;
import com.opengamma.strata.basics.index.OvernightIndex;
import com.opengamma.strata.basics.index.OvernightIndices;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.product.fx.type.FxSwapConvention;
import com.opengamma.strata.product.fx.type.ImmutableFxSwapConvention;
import com.opengamma.strata.product.index.type.IborFutureContractSpec;
import com.opengamma.strata.product.index.type.ImmutableIborFutureContractSpec;
import com.opengamma.strata.product.swap.OvernightAccrualMethod;
import com.opengamma.strata.product.swap.type.FixedIborSwapConvention;
import com.opengamma.strata.product.swap.type.FixedOvernightSwapConvention;
import com.opengamma.strata.product.swap.type.FixedRateSwapLegConvention;
import com.opengamma.strata.product.swap.type.IborIborSwapConvention;
import com.opengamma.strata.product.swap.type.IborRateSwapLegConvention;
import com.opengamma.strata.product.swap.type.ImmutableFixedIborSwapConvention;
import com.opengamma.strata.product.swap.type.ImmutableFixedOvernightSwapConvention;
import com.opengamma.strata.product.swap.type.ImmutableIborIborSwapConvention;
import com.opengamma.strata.product.swap.type.ImmutableOvernightIborSwapConvention;
import com.opengamma.strata.product.swap.type.ImmutableThreeLegBasisSwapConvention;
import com.opengamma.strata.product.swap.type.ImmutableXCcyOvernightOvernightSwapConvention;
import com.opengamma.strata.product.swap.type.OvernightIborSwapConvention;
import com.opengamma.strata.product.swap.type.OvernightRateSwapLegConvention;
import com.opengamma.strata.product.swap.type.ThreeLegBasisSwapConvention;
import com.opengamma.strata.product.swap.type.XCcyOvernightOvernightSwapConvention;

public class Conventions {
  private static final HolidayCalendarId GBLO_AUSY = AUSY;
  
  
  public static final FixedIborSwapConvention AUD_FIXED_3M_BBSW_3M =  
      ImmutableFixedIborSwapConvention.of(
          "AUD-FIXED-3M-BBSW-3M",
          FixedRateSwapLegConvention.of(AUD, ACT_365_ACTUAL, P3M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M));
  
  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_6M =
      ImmutableFixedIborSwapConvention.of(
          "AUD-FIXED-6M-BBSW-6M",
          FixedRateSwapLegConvention.of(AUD, ACT_365_ACTUAL, P6M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
  
  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_3M =
      ImmutableFixedIborSwapConvention.of(
          "AUD-FIXED-6M-BBSW-3M",
          FixedRateSwapLegConvention.of(AUD, ACT_365_ACTUAL, P6M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M));
  
  public static final FixedIborSwapConvention AUD_FIXED_3M_BBSW_6M =
      ImmutableFixedIborSwapConvention.of(
          "AUD-FIXED-6M-BBSW-3M",
          FixedRateSwapLegConvention.of(AUD, ACT_365_ACTUAL, P3M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
  
  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_3M_ASW =
      ImmutableFixedIborSwapConvention.of(
          "AUD-FIXED-6M-BBSW-6M-ASW",
          FixedRateSwapLegConvention.of(AUD, DayCounts.ACT_ACT_ISDA, P6M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),          
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M));  
  
  
//  public static final FixedIborSwapConvention AUD_FIXED_3M_BBSW_3M =
//      ImmutableFixedIborSwapConvention.of(
//          "AUD-FIXED-3M-BBSW-3M",
//          FixedRateSwapLegConvention.builder()
//            .currency(AUD)
//            .dayCount(ACT_365_ACTUAL)
//            .accrualFrequency(P3M)
//            .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(FOLLOWING, GBLO_AUSY))
//            .stubConvention(StubConvention.SMART_INITIAL)
//            .build(),          
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
//          DaysAdjustment.ofBusinessDays(1, GBLO_AUSY));  
//  
//  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_6M =
//      ImmutableFixedIborSwapConvention.of(
//          "AUD-FIXED-6M-BBSW-6M",
//          FixedRateSwapLegConvention.builder()
//            .currency(AUD)
//            .dayCount(ACT_365_ACTUAL)
//            .accrualFrequency(P6M)
//            .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(FOLLOWING, GBLO_AUSY))
//            .stubConvention(StubConvention.SMART_INITIAL)
//            .build(),          
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M),
//          DaysAdjustment.ofBusinessDays(1, GBLO_AUSY));  
//  
//  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_3M =
//      ImmutableFixedIborSwapConvention.of(
//          "AUD-FIXED-6M-BBSW-3M",
//          FixedRateSwapLegConvention.builder()
//            .currency(AUD)
//            .dayCount(ACT_365_ACTUAL)
//            .accrualFrequency(P6M)
//            .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(FOLLOWING, GBLO_AUSY))
//            .stubConvention(StubConvention.SMART_INITIAL)
//            .build(),          
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
//          DaysAdjustment.ofBusinessDays(1, GBLO_AUSY));  
//  
//  public static final FixedIborSwapConvention AUD_FIXED_3M_BBSW_6M =
//      ImmutableFixedIborSwapConvention.of(
//          "AUD-FIXED-3M-BBSW-6M",
//          FixedRateSwapLegConvention.builder()
//            .currency(AUD)
//            .dayCount(ACT_365_ACTUAL)
//            .accrualFrequency(P3M)
//            .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(FOLLOWING, GBLO_AUSY))
//            .stubConvention(StubConvention.SMART_INITIAL)
//            .build(),          
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M),
//          DaysAdjustment.ofBusinessDays(1, GBLO_AUSY));  
//  
//  public static final FixedIborSwapConvention AUD_FIXED_6M_BBSW_3M_ASW =
//      ImmutableFixedIborSwapConvention.of(
//          "AUD-FIXED-6M-BBSW-6M-ASW",
//          FixedRateSwapLegConvention.builder()
//            .currency(AUD)
//            .dayCount(DayCounts.ACT_ACT_ISDA)
//            .accrualFrequency(P6M)
//            .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(FOLLOWING, GBLO_AUSY))
//            .stubConvention(StubConvention.SMART_INITIAL)
//            .build(),          
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
//          DaysAdjustment.ofBusinessDays(1, GBLO_AUSY));  
  
  public static FixedIborSwapConvention makeFixedIborSwapConvention()
  {
    return null;    
  }
  
  /**
   * AUD fixed vs AONIA OIS swap for terms less than or equal to one year.
   * <p>
   * Both legs pay once at the end and use day count 'Act/360'.
   * The spot date offset is 1 days and the payment date offset is 2 days.
   */
  public static final DaysAdjustment paymentDateOffset = DaysAdjustment.ofBusinessDays(2, GBLO_AUSY);
  public static final DaysAdjustment spotDateOffset = DaysAdjustment.ofBusinessDays(1, GBLO_AUSY);
  
  public static final FixedOvernightSwapConvention AUD_FIXED_TERM_AONIA_OIS =
      ImmutableFixedOvernightSwapConvention.of(
          "AUD-FIXED-TERM-AONIA-OIS",
          FixedRateSwapLegConvention.builder()
              .currency(AUD_AONIA.getCurrency())
              .dayCount(ACT_365_ACTUAL)//ACT_360
              .accrualFrequency(TERM)              
              .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY))
              .paymentFrequency(TERM)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)
              .build(),
              
          OvernightRateSwapLegConvention.builder()
              .index(AUD_AONIA)
              .accrualMethod(COMPOUNDED)
              .accrualFrequency(TERM)
              .paymentFrequency(TERM)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)
              .build(),
          spotDateOffset);
  
  
  /**
   * AUD fixed vs AONIA OIS swap for terms greater than one year.
   * <p>
   * Both legs pay annually and use day count 'Act/360'.
   * The spot date offset is 2 days and the payment date offset is 2 days.
   */
  public static final FixedOvernightSwapConvention AUD_FIXED_3M_AONIA_OIS =
      ImmutableFixedOvernightSwapConvention.of(
          "AUD-FIXED-3M-AONIA-OIS",
          FixedRateSwapLegConvention.builder()
              .currency(AUD_AONIA.getCurrency())
              .dayCount(ACT_365_ACTUAL)
              .accrualFrequency(P3M)
              .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY))
              .paymentFrequency(P3M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)              
              .build(),
          OvernightRateSwapLegConvention.builder()
              .index(AUD_AONIA)
              .accrualMethod(COMPOUNDED)
              .accrualFrequency(P3M)
              .paymentFrequency(P3M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)
              .build(),
          spotDateOffset);
  
  public static final FixedOvernightSwapConvention AUD_FIXED_6M_AONIA_OIS_ASW =
      ImmutableFixedOvernightSwapConvention.of(
          "AUD-FIXED-6M-AONIA-OIS",
          FixedRateSwapLegConvention.builder()
              .currency(AUD_AONIA.getCurrency())
              .dayCount(DayCounts.ACT_ACT_ISDA)
              .accrualFrequency(P6M)
              .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY))
              .paymentFrequency(P6M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)              
              .build(),
          OvernightRateSwapLegConvention.builder()
              .index(AUD_AONIA)
              .accrualMethod(COMPOUNDED)
              .accrualFrequency(P3M)
              .paymentFrequency(P3M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)
              .build(),
          spotDateOffset);
  
  
  public static final FixedOvernightSwapConvention AUD_FIXED_12M_AONIA_OIS =
      ImmutableFixedOvernightSwapConvention.of(
          "AUD-FIXED-12M-AONIA-OIS",
          FixedRateSwapLegConvention.builder()
              .currency(AUD_AONIA.getCurrency())
              .dayCount(ACT_365_ACTUAL)
              .accrualFrequency(P12M)
              .accrualBusinessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY))
              .paymentFrequency(P12M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)              
              .build(),
          OvernightRateSwapLegConvention.builder()
              .index(AUD_AONIA)
              .accrualMethod(COMPOUNDED)
              .accrualFrequency(P12M)
              .paymentFrequency(P12M)
              .paymentDateOffset(paymentDateOffset)
              .stubConvention(StubConvention.SMART_INITIAL)
              .build(),
          spotDateOffset);
  //* AUD BBSW3M vs BBSW6M basis swap
//  public static final IborIborSwapConvention AUD_BBSW_3M_BBSW_6M =
//      ImmutableIborIborSwapConvention.of(
//          "AUD-BBSW-3M-BBSW-6M",
//          IborRateSwapLegConvention.builder()
//              .index(IborIndices.AUD_BBSW_3M)
//              .paymentFrequency(Frequency.P3M)
//              .compoundingMethod(CompoundingMethod.FLAT)
//              .stubConvention(StubConvention.SMART_INITIAL) 
//              .build(),
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
  
  public static final IborIborSwapConvention AUD_BBSW_3M_BBSW_6M =
      ImmutableIborIborSwapConvention.of(
          "AUD-BBSW-3M-BBSW-6M",
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
  
  public static final ThreeLegBasisSwapConvention AUD_FIXED_1Y_BBSW_3M_BBSW_6M =
      ImmutableThreeLegBasisSwapConvention.of(
          "AUD-FIXED-1Y-BBSW-3M-BBSW-6M",
          FixedRateSwapLegConvention.of(AUD, IborIndices.AUD_BBSW_3M.getDayCount(), P12M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
  
  
//  public static final ThreeLegBasisSwapConvention AUD_FIXED_1Y_BBSW_3M_BBSW_6M =
//      ImmutableThreeLegBasisSwapConvention.of(
//          "AUD-FIXED-3Y-BBSW-3M-BBSW-6M",
//          FixedRateSwapLegConvention.of(AUD, ACT_365_ACTUAL, P3M, BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY)),
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_3M),
//          IborRateSwapLegConvention.of(IborIndices.AUD_BBSW_6M));
//  
  
  public static final OvernightIborSwapConvention AUD_AONIA_3M_BBSW_3M =
      makeConvention("AUD-AONIA-3M-BBSW-3M", AUD_AONIA, IborIndices.AUD_BBSW_3M, ACT_365F, P3M, 0, 0, COMPOUNDED);
  
  public static final OvernightIborSwapConvention AUD_AONIA_6M_BBSW_6M =
      makeConvention("AUD-AONIA-6M-BBSW-6M", AUD_AONIA, IborIndices.AUD_BBSW_6M, ACT_365F, P6M, 0, 0, COMPOUNDED);
  
  public static final OvernightIborSwapConvention AUD_AONIA_6M_BBSW_3M =
      makeConvention("AUD-AONIA-6M-BBSW-3M", AUD_AONIA, IborIndices.AUD_BBSW_3M, ACT_365F, P6M, 0, 0, COMPOUNDED);
  
  public static final OvernightIborSwapConvention AUD_AONIA_3M_BBSW_6M =
      makeConvention("AUD-AONIA-6M-BBSW-3M", AUD_AONIA, IborIndices.AUD_BBSW_6M, ACT_365F, P3M, 0, 0, COMPOUNDED);
  
  public static OvernightIborSwapConvention makeConvention(
      String name,
      OvernightIndex onIndex,
      IborIndex iborIndex,
      DayCount dayCount,
      Frequency frequency,
      int paymentLag,
      int cutOffDays,
      OvernightAccrualMethod accrual) {

    HolidayCalendarId calendarOn = onIndex.getFixingCalendar();
    DaysAdjustment paymentDateOffset = DaysAdjustment.ofBusinessDays(paymentLag, calendarOn);
    return ImmutableOvernightIborSwapConvention.of(
        name,
        OvernightRateSwapLegConvention.builder()
            .index(onIndex)
            .accrualMethod(accrual)
            .accrualFrequency(frequency)
            .paymentFrequency(frequency)
            .paymentDateOffset(paymentDateOffset)
            .stubConvention(StubConvention.SMART_INITIAL)
            .rateCutOffDays(cutOffDays)
            .build(),            
        IborRateSwapLegConvention.of(iborIndex));
  }
  
 
  
  
//  public static final OvernightIborSwapConvention AUD_REPO_3M_BBSW_3M =
//      makeConvention("AUD-AONIA-3M-REPO-3M", AUD_AONIA, IborIndices.AUD_BBSW_3M, ACT_365F, P3M, 0, 0, COMPOUNDED);
  
  
  public static final IborFutureContractSpec AUD_BBSW_3M_IMM_ASX =
      ImmutableIborFutureContractSpec.builder()
          .name("AUD-BBSW-3M-IMM-ASX")
          .index(IborIndices.AUD_BBSW_3M)
          .dateSequence(QUARTERLY_10TH)
          .notional(1000_000d)
          .build();  
    
  public static final XCcyOvernightOvernightSwapConvention AUD_AONIA_3M_SOFR_3M = 
      makeXCCYConvention("AUD-AONIA-3M-USD-SOFR-3M", AUD_AONIA, OvernightIndices.USD_SOFR, ACT_365F, P3M, 0, 0, COMPOUNDED);
  public static XCcyOvernightOvernightSwapConvention makeXCCYConvention(
      String name,
      OvernightIndex on1Index,
      OvernightIndex on2Index,      
      DayCount dayCount,
      Frequency frequency,
      int paymentLag,
      int cutOffDays,
      OvernightAccrualMethod accrual) {
    HolidayCalendarId calendarOn = on1Index.getFixingCalendar();
    DaysAdjustment paymentDateOffset = DaysAdjustment.ofBusinessDays(paymentLag, calendarOn);
    return ImmutableXCcyOvernightOvernightSwapConvention.of(
        name,
        OvernightRateSwapLegConvention.builder()
            .index(on1Index)
            .accrualMethod(accrual)
            .accrualFrequency(frequency)
            .paymentFrequency(frequency)
            .paymentDateOffset(paymentDateOffset)
            .stubConvention(StubConvention.SMART_INITIAL)
            .rateCutOffDays(cutOffDays)
            .build(),            
        OvernightRateSwapLegConvention.builder()
            .index(on2Index)
            .accrualMethod(accrual)
            .accrualFrequency(frequency)
            .paymentFrequency(frequency)
            .paymentDateOffset(paymentDateOffset)
            .stubConvention(StubConvention.SMART_INITIAL)
            .rateCutOffDays(cutOffDays)
            .build(),
       paymentDateOffset);
  }
  
  
  public static final FxSwapConvention AUD_USD = 
      ImmutableFxSwapConvention.builder()        
        .name("AUD/USD")
        .currencyPair(CurrencyPair.of(AUD, USD))
        .businessDayAdjustment(BusinessDayAdjustment.of(MODIFIED_FOLLOWING, GBLO_AUSY))
        .spotDateOffset(DaysAdjustment.ofBusinessDays(1, GBLO_AUSY))
        .build();
}
