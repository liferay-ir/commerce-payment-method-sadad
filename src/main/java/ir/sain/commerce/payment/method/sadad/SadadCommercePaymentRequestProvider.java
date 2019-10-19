/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package ir.sain.commerce.payment.method.sadad;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.payment.request.CommercePaymentRequest;
import com.liferay.commerce.payment.request.CommercePaymentRequestProvider;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author Nader Jafari
 */
@Component(
        immediate = true,
        property = "commerce.payment.engine.method.key=" + SadadCommercePaymentMethod.KEY,
        service = CommercePaymentRequestProvider.class
)

public class SadadCommercePaymentRequestProvider
        implements CommercePaymentRequestProvider {

    @Override
    public CommercePaymentRequest getCommercePaymentRequest(
            String cancelUrl, long commerceOrderId,
            HttpServletRequest httpServletRequest, Locale locale,
            String returnUrl, String transactionId)
            throws PortalException {

        CommerceOrder commerceOrder =
                _commerceOrderLocalService.getCommerceOrder(commerceOrderId);

        return new SadadCommercePaymentRequest(
                commerceOrder.getTotal(), cancelUrl, commerceOrderId, locale,
                httpServletRequest, returnUrl, transactionId);
    }

    @Reference
    private CommerceOrderLocalService _commerceOrderLocalService;

}