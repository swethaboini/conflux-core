package org.mifosplatform.portfolio.collectionsheet.command;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;

public class SingleChargeRepaymentCommand {
	private final Long clientId;
    private final Long chargeId;
    private final BigDecimal transactionAmount;
    
    
    public SingleChargeRepaymentCommand(Long clientId, Long chargeId,
			BigDecimal transactionAmount) {
		this.clientId = clientId;
		this.chargeId = chargeId;
		this.transactionAmount = transactionAmount;
	}
    
	public Long getClientId() {
		return this.clientId;
	}

	public Long getChargeId() {
		return this.chargeId;
	}

	public BigDecimal getTransactionAmount() {
		return this.transactionAmount;
	}

	
   
    
    
	
}
