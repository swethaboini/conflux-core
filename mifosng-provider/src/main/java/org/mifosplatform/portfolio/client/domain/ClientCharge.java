/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.OrganisationCurrency;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client_charge")
public class ClientCharge extends AbstractPersistable<Long> {

	@ManyToOne(optional = false)
	@JoinColumn(name = "client_id", referencedColumnName = "id", nullable = false)
	private Client client;

	@ManyToOne(optional = false)
	@JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
	private Charge charge;

	@Column(name = "charge_time_enum", nullable = false)
	private Integer chargeTime;

	@Temporal(TemporalType.DATE)
	@Column(name = "charge_due_date")
	private Date dueDate;

	@Column(name = "charge_calculation_enum")
	private Integer chargeCalculation;

	@Column(name = "amount", scale = 6, precision = 19, nullable = false)
	private BigDecimal amount;

	@Column(name = "amount_paid_derived", scale = 6, precision = 19, nullable = true)
	private BigDecimal amountPaid;

	@Column(name = "amount_waived_derived", scale = 6, precision = 19, nullable = true)
	private BigDecimal amountWaived;

	@Column(name = "amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
	private BigDecimal amountWrittenOff;

	@Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
	private BigDecimal amountOutstanding;

	@Column(name = "is_penalty", nullable = false)
	private boolean penaltyCharge = false;

	@Column(name = "is_paid_derived", nullable = false)
	private boolean paid = false;

	@Column(name = "waived", nullable = false)
	private boolean waived = false;

	@Column(name = "is_active", nullable = false)
	private boolean status = true;

	@Temporal(TemporalType.DATE)
	@Column(name = "inactivated_on_date")
	private Date inactivationDate;

	@ManyToOne
	@JoinColumn(name = "client_recurring_charge_id", referencedColumnName = "id", nullable = false)
	private ClientRecurringCharge clientRecurringCharge;

	public ClientRecurringCharge getClientRecurringCharge() {
		return this.clientRecurringCharge;
	}

	@Transient
	private OrganisationCurrency currency;

	protected ClientCharge() {
		//
	}

	public static ClientCharge createNew(final Client client,
			final Charge charge, final JsonCommand command) {
		BigDecimal amount = command
				.bigDecimalValueOfParameterNamed(ClientApiConstants.amountParamName);
		final LocalDate dueDate = command
				.localDateValueOfParameterNamed(ClientApiConstants.dueAsOfDateParamName);
		final boolean status = true;
		// Derive from charge definition if not passed in as a parameter
		amount = (amount == null) ? charge.getAmount() : amount;
		return new ClientCharge(client, charge, amount, dueDate, status);
	}
	

	private ClientCharge(final Client client, final Charge charge,
			final BigDecimal amount, final LocalDate dueDate,
			final boolean status) {

		this.client = client;
		this.charge = charge;
		this.penaltyCharge = charge.isPenalty();
		this.chargeTime = charge.getChargeTimeType();
		this.dueDate = (dueDate == null) ? null : dueDate.toDate();
		this.chargeCalculation = charge.getChargeCalculation();

		BigDecimal chargeAmount = charge.getAmount();
		if (amount != null) {
			chargeAmount = amount;
		}

		populateDerivedFields(chargeAmount);

		this.paid = determineIfFullyPaid();
		this.status = status;
	}

	public Money pay(final Money amountPaid) {
		Money amountPaidToDate = Money.of(this.getCurrency(), this.amountPaid);
		Money amountOutstanding = Money.of(this.getCurrency(),
				this.amountOutstanding);
		amountPaidToDate = amountPaidToDate.plus(amountPaid);
		amountOutstanding = amountOutstanding.minus(amountPaid);
		this.amountPaid = amountPaidToDate.getAmount();
		this.amountOutstanding = amountOutstanding.getAmount();
		this.paid = determineIfFullyPaid();
		return Money.of(this.getCurrency(), this.amountOutstanding);
	}

	public void undoPayment(final Money transactionAmount) {
		Money amountPaid = getAmountPaid();
		amountPaid = amountPaid.minus(transactionAmount);
		this.amountPaid = amountPaid.getAmount();
		this.amountOutstanding = calculateOutstanding();
		this.paid = false;
		this.status = true;
	}

	public Money waive() {
		Money amountWaivedToDate = getAmountWaived();
		Money amountOutstanding = getAmountOutstanding();
		Money totalAmountWaived = amountWaivedToDate.plus(amountOutstanding);
		this.amountWaived = totalAmountWaived.getAmount();
		this.amountOutstanding = BigDecimal.ZERO;
		this.waived = true;
		return totalAmountWaived;
	}

	public void undoWaiver(final Money transactionAmount) {
		Money amountWaived = getAmountWaived();
		amountWaived = amountWaived.minus(transactionAmount);
		this.amountWaived = amountWaived.getAmount();
		this.amountOutstanding = calculateOutstanding();
		this.waived = false;
		this.status = true;
	}

	private void populateDerivedFields(final BigDecimal amount) {
		switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
		case INVALID:
			this.amount = null;
			this.amountPaid = null;
			this.amountOutstanding = BigDecimal.ZERO;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		case FLAT:
			this.amount = amount;
			this.amountPaid = null;
			this.amountOutstanding = amount;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		default:
			break;
		}
	}

	public boolean isOnSpecifiedDueDate() {
		return ChargeTimeType.fromInt(this.chargeTime).isOnSpecifiedDueDate();
	}

	private boolean determineIfFullyPaid() {
		return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
	}

	private BigDecimal calculateOutstanding() {
		BigDecimal amountPaidLocal = BigDecimal.ZERO;
		if (this.amountPaid != null) {
			amountPaidLocal = this.amountPaid;
		}

		BigDecimal amountWaivedLocal = BigDecimal.ZERO;
		if (this.amountWaived != null) {
			amountWaivedLocal = this.amountWaived;
		}

		BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
		if (this.amountWrittenOff != null) {
			amountWrittenOffLocal = this.amountWrittenOff;
		}

		final BigDecimal totalAccountedFor = amountPaidLocal.add(
				amountWaivedLocal).add(amountWrittenOffLocal);

		return this.amount.subtract(totalAccountedFor);
	}

	public LocalDate getDueLocalDate() {
	//	LocalDate dueDate = null;
		/*if (this.dueDate != null) {
			dueDate = ;
		}*/
		return new LocalDate(this.dueDate);
	}

	public Client getClient() {
		return this.client;
	}

	public Charge getCharge() {
		return this.charge;
	}

	public Integer getChargeTime() {
		return this.chargeTime;
	}

	public Date getDueDate() {
		return this.dueDate;
	}

	public Integer getChargeCalculation() {
		return this.chargeCalculation;
	}

	public boolean isPenaltyCharge() {
		return this.penaltyCharge;
	}

	public boolean isPaid() {
		return this.paid;
	}

	public boolean isWaived() {
		return this.waived;
	}

	public boolean isActive() {
		return this.status;
	}

	public boolean isNotActive() {
		return !this.status;
	}

	public Date getInactivationDate() {
		return this.inactivationDate;
	}

	public Long getClientId() {
		return client.getId();
	}

	public Long getOfficeId() {
		return this.client.getOffice().getId();
	}

	public void setCurrency(OrganisationCurrency currency) {
		this.currency = currency;
	}

	public MonetaryCurrency getCurrency() {
		return this.currency.toMonetaryCurrency();
	}

	public boolean isPaidOrPartiallyPaid(final MonetaryCurrency currency) {
		final Money amountWaivedOrWrittenOff = getAmountWaived().plus(
				getAmountWrittenOff());
		return Money.of(currency, this.amountPaid)
				.plus(amountWaivedOrWrittenOff).isGreaterThanZero();
	}

	public Money getAmount() {
		return Money.of(getCurrency(), this.amount);
	}

	public Money getAmountPaid() {
		return Money.of(getCurrency(), this.amountPaid);
	}

	public Money getAmountWaived() {
		return Money.of(getCurrency(), this.amountWaived);
	}

	public Money getAmountWrittenOff() {
		return Money.of(getCurrency(), this.amountWrittenOff);
	}
	
	public BigDecimal getOutStanding(){
		return this.amountOutstanding;
	}

	public Money getAmountOutstanding() {
		return Money.of(getCurrency(), this.amountOutstanding);
	}

}
