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

package ir.sain.commerce.payment.method.sadad.servlet.filter;

import com.liferay.portal.servlet.filters.authverifier.AuthVerifierFilter;
import ir.sain.commerce.payment.method.sadad.constants.SadadCommercePaymentMethodConstants;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Filter;

/**
 * @author Nader Jafari
 */
@Component(
	immediate = true,
	property = {
		"filter.init.auth.verifier.PortalSessionAuthVerifier.urls.includes=/" + SadadCommercePaymentMethodConstants.SERVLET_PATH + "/*",
		"osgi.http.whiteboard.filter.name=ir.sain.commerce.payment.method.sadad.servlet.filter.CommercePaymentMethodSadadAuthVerifierFilter",
		"osgi.http.whiteboard.servlet.pattern=/" + SadadCommercePaymentMethodConstants.SERVLET_PATH + "/*"
	},
	service = Filter.class
)
public class CommercePaymentMethodSadadAuthVerifierFilter
	extends AuthVerifierFilter {
}