package org.mifosplatform.portfolio.collectionsheet.command;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;

public class CollectionSheetChargeRepaymentCommand {
	 private final String dateFormat;
	 private final LocalDate transactionDate;
	 private final Locale locale;
    private final SingleChargeRepaymentCommand[] chargeTransactions;
    
	public CollectionSheetChargeRepaymentCommand(String dateFormat,
			Locale locale,LocalDate transactionDate, SingleChargeRepaymentCommand[] chargeTransactions) {
		this.dateFormat = dateFormat;
		this.transactionDate = transactionDate;
		this.locale = locale;
		this.chargeTransactions = chargeTransactions;
	}

	public LocalDate getTransactionDate() {
		return this.transactionDate;
	}

	public String getDateFormat() {
		return this.dateFormat;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public SingleChargeRepaymentCommand[] getChargeTransactions() {
		return this.chargeTransactions;
	}
   


}