package org.mifosplatform.portfolio.collectionsheet.serialization;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.CollectionSheetChargeRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleChargeRepaymentCommand;
import org.mifosplatform.portfolio.collectionsheet.command.SingleDisbursalCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class CollectionSheetChargeRepaymentCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CollectionSheetChargeRepaymentCommand>{

	private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollectionSheetChargeRepaymentCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }
    
    @Override
    public CollectionSheetChargeRepaymentCommand commandFromApiJson(final String json) {
    	if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
    	
    	final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();

        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);
        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed("transactionDate", element);
        final String dateFormat = this.fromApiJsonHelper.extractStringNamed("dateFormat", element);
        SingleChargeRepaymentCommand[] chargeTransactions = null;
        
        if (element.isJsonObject()) {
            if (topLevelJsonElement.has("chargeTransactions")
                    && topLevelJsonElement.get("chargeTransactions").isJsonArray()) {
                final JsonArray array = topLevelJsonElement.get("chargeTransactions").getAsJsonArray();
                chargeTransactions = new SingleChargeRepaymentCommand[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    final JsonObject chargeTransactionElement = array.get(i).getAsJsonObject();

                    final Long clientId = this.fromApiJsonHelper.extractLongNamed("clientId", chargeTransactionElement);
                    final Long chargeId = this.fromApiJsonHelper.extractLongNamed("chargeId", chargeTransactionElement);
                    final BigDecimal chargeAmount = this.fromApiJsonHelper.extractBigDecimalNamed("transactionAmount",
                            chargeTransactionElement, locale);
                   chargeTransactions[i] = new SingleChargeRepaymentCommand(clientId, chargeId, chargeAmount);
                }
            }
            
        }
    	return new CollectionSheetChargeRepaymentCommand(dateFormat,locale,transactionDate,chargeTransactions);
    }
}
