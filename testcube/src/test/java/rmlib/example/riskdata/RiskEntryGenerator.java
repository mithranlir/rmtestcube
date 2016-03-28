package rmlib.example.riskdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * For a given trade id, provides a list of risk entries to be computed for that trade.
 *
 * @author Quartet Financial Systems
 */
public class RiskEntryGenerator {

	/** Number of asOfDate to calculate for */
	protected int historicalDates = 0;

	public RiskEntryGenerator(int historicalDates) {
		setHistoricalDates(historicalDates);
	}

	/**
	 * Get the number of historical dates
	 * @return the number of historical dates
	 */
	public int getHistoricalDates() { return this.historicalDates; }

	/**
	 * Set the number of historical dates
	 * @param historicalDates the number of historical dates to set
	 */
	public void setHistoricalDates(int historicalDates) { this.historicalDates = historicalDates; }

	/**
	 * Generates the risks for a specific trade
	 * @param tradeId the id of the trade to generate risks for
	 * @return the risks generated for the input trade
	 */
	public List<RiskEntry> generateRiskEntries(long tradeId) {

		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date curDate = null;
		try {
			curDate = sdf.parse("2016-01-01");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		final Calendar calendar = CALENDAR.get();

		List<RiskEntry> risks = new ArrayList<>(this.historicalDates + 1);
		for(int i = 0; i <= this.historicalDates; i++) {

			calendar.setTimeInMillis(curDate.getTime());
			calendar.add(Calendar.DAY_OF_MONTH, -i);
			Date asOfDate = calendar.getTime();

			//instantiate the result that will hold the enrichment data
			RiskEntry result = new RiskEntry();

			//same for all calculated measures
			result.setTradeId(tradeId);
			result.setAsOfDate(asOfDate);
			risks.add(result);
		}

		return risks;
	}


	/** Reusable, thread safe calendar */
	static final ThreadLocal<Calendar> CALENDAR = new ThreadLocal<Calendar>() {
		@Override
		public Calendar initialValue() {
			return Calendar.getInstance();
		}
	};

}