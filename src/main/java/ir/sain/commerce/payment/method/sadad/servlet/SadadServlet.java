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

package ir.sain.commerce.payment.method.sadad.servlet;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.payment.engine.CommercePaymentEngine;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.servlet.PortalSessionThreadLocal;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import ir.sain.commerce.payment.method.sadad.configuration.SadadGroupServiceConfiguration;
import ir.sain.commerce.payment.method.sadad.constants.SadadCommercePaymentMethodConstants;
import ir.sain.commerce.payment.method.sadad.util.SadadWebserviceHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Nader Jafari
 */
@Component(
        immediate = true,
        property = {
                "osgi.http.whiteboard.context.path=/" + SadadCommercePaymentMethodConstants.SERVLET_PATH,
                "osgi.http.whiteboard.servlet.name=ir.sain.commerce.payment.method.sadad.servlet.SadadServlet",
                "osgi.http.whiteboard.servlet.pattern=/" + SadadCommercePaymentMethodConstants.SERVLET_PATH + "/*"
        },
        service = Servlet.class
)
public class SadadServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws IOException, ServletException {

        try {
            HttpSession httpSession = httpServletRequest.getSession();

            if (PortalSessionThreadLocal.getHttpSession() == null) {
                PortalSessionThreadLocal.setHttpSession(httpSession);
            }

            User user = _portal.getUser(httpServletRequest);

            PermissionChecker permissionChecker =
                    PermissionCheckerFactoryUtil.create(user);

            PermissionThreadLocal.setPermissionChecker(permissionChecker);

            RequestDispatcher requestDispatcher =
                    _servletContext.getRequestDispatcher(
                            "/irankish_form/irankish-form.jsp");

            requestDispatcher.forward(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            _portal.sendError(e, httpServletRequest, httpServletResponse);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse)
            throws IOException, ServletException {

        try {
            long groupId = ParamUtil.getLong(httpServletRequest, "groupId");
            String uuid = ParamUtil.getString(httpServletRequest, "uuid");
            String resOrderId = ParamUtil.getString(
                    httpServletRequest, "OrderId");
            String resultCode = ParamUtil.getString(
                    httpServletRequest, "ResCode");
            String token = ParamUtil.getString(httpServletRequest, "token");
            System.out.println("token Back ---------- = " + token);
            System.out.println("resultCode  Back ---------- = " + resultCode);
            System.out.println("resOrderId  Back ---------- = " + resOrderId);
            String redirect = ParamUtil.getString(
                    httpServletRequest, "redirect");

            System.out.println("redirect = " + redirect);

            CommerceOrder commerceOrder =
                    _commerceOrderLocalService.getCommerceOrderByUuidAndGroupId(
                            uuid, groupId);

            SadadGroupServiceConfiguration sadadGroupServiceConfiguration =
                    _getConfiguration(commerceOrder.getGroupId());

            StringBuilder transactionReference = new StringBuilder();
            transactionReference.append(commerceOrder.getCompanyId());
            transactionReference.append(commerceOrder.getGroupId());
            transactionReference.append(commerceOrder.getCommerceOrderId());

            if (resultCode.equals("0")) {

                String[] responseVerifyArray = SadadWebserviceHelper.melliVerify(token, sadadGroupServiceConfiguration.merchantKey());
                String verifyResultCode = responseVerifyArray[0];
                boolean verifySuccess = resultCode.equals("0");

                System.out.println("verifyResultCode = " + verifyResultCode);
                System.out.println("verifySuccess = " + verifySuccess);


                if (verifySuccess) {
                    _commercePaymentEngine.completePayment(
                            commerceOrder.getCommerceOrderId(),
                            transactionReference.toString(), httpServletRequest);
                } else {
                    _commercePaymentEngine.cancelPayment(
                            commerceOrder.getCommerceOrderId(),
                            transactionReference.toString(), httpServletRequest);
                }
            } else {
                _commercePaymentEngine.cancelPayment(
                        commerceOrder.getCommerceOrderId(),
                        transactionReference.toString(), httpServletRequest);
            }
            httpServletResponse.sendRedirect(redirect);

        } catch (Exception e) {
            e.printStackTrace();
            _portal.sendError(e, httpServletRequest, httpServletResponse);
        }
    }

    private SadadGroupServiceConfiguration _getConfiguration(long groupId)
            throws ConfigurationException {

        return _configurationProvider.getConfiguration(
                SadadGroupServiceConfiguration.class,
                new GroupServiceSettingsLocator(
                        groupId, SadadCommercePaymentMethodConstants.SERVICE_NAME));
    }

    @Reference
    private CommerceOrderLocalService _commerceOrderLocalService;

    @Reference
    private CommercePaymentEngine _commercePaymentEngine;

    @Reference
    private ConfigurationProvider _configurationProvider;

    @Reference
    private Portal _portal;

    @Reference(
            target = "(osgi.web.symbolicname=ir.sain.commerce.payment.method.sadad)"
    )
    private ServletContext _servletContext;

}