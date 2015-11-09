/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.client.data.ClientChargeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientChargeRepository extends	JpaRepository<ClientCharge, Long>,	JpaSpecificationExecutor<ClientCharge> {

	@Query("from ClientCharge cc where cc.clientRecurringCharge.id = :recurringId")
	Collection<ClientCharge> findClientChargeByRecurringId(
			@Param("recurringId") Long recurringId);
	

}
