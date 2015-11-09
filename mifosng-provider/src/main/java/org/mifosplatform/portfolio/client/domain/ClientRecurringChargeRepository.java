/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;


import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRecurringChargeRepository extends
		JpaRepository<ClientRecurringCharge, Long>,
		JpaSpecificationExecutor<ClientRecurringCharge> {

	@Query("from ClientRecurringCharge")
	Collection<ClientRecurringCharge> findClientRecurringCharges();
	


}