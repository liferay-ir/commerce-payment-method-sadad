/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package ir.sain.commerce.payment.method.sadad.configuration;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Nader Jafari
 */
@ExtendedObjectClassDefinition(
	category = "payment", scope = ExtendedObjectClassDefinition.Scope.GROUP
)
@Meta.OCD(
	id = "ir.sain.commerce.payment.method.sadad.configuration.SadadGroupServiceConfiguration",
	localization = "content/Language",
	name = "commerce-payment-method-sadad-group-service-configuration-name"
)
public interface SadadGroupServiceConfiguration {

	@Meta.AD(name = "merchant-id", required = false)
	public String merchantId();

	@Meta.AD(name = "merchant-key", required = false)
	public String merchantKey();

	@Meta.AD(name = "terminal-id", required = false)
	public String terminalId();

}