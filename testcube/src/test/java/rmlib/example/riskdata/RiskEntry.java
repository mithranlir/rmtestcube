package rmlib.example.riskdata;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Risk entry record, to indicate a risk to be computed.
 * It contains a reference to a trade, and a date.
 * The values of the risk are computed through a store trigger.
 *
 * @author Quartet FS
 *
 */
public class RiskEntry {

	/** The host name of the JVM */
	public static final String HOST_NAME = ManagementFactory.getRuntimeMXBean().getName();

	/**
	 * The pattern to parse/format dates.
	 */
	public static final String DATE_PATTERN = "yyyy.MM.dd-HH:mm:ss.SSS";

	/**
	 * The CSV format to parse/format dates.
	 */
	public static final ThreadLocal<SimpleDateFormat> RISK_CSV_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(DATE_PATTERN);
		}
	};

	private long tradeId;
	private Date asOfDate;

	/** Empty constructor */
	public RiskEntry() { };

	public long getTradeId() { return this.tradeId; }

	public void setTradeId(long tradeId) { this.tradeId = tradeId; }

	public String getHostName() { return HOST_NAME; }

	public Date getAsOfDate() { return this.asOfDate; }

	public void setAsOfDate(Date asOfDate) { this.asOfDate = asOfDate; }

	/**
	 * Compute a CSV representation of this object. For simplier loading of the
	 * CSV files, the fields are aligned with the one in the datastore.
	 *
	 * @return A CSV String representing this object.
	 */
	public String toCsvString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getTradeId());
		sb.append(';').append(RISK_CSV_DATE_FORMAT.get().format(getAsOfDate()));
		return sb.toString();
	}

}