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

package ir.sain.commerce.payment.method.sadad.portlet.action;

import com.liferay.commerce.admin.constants.CommerceAdminPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import ir.sain.commerce.payment.method.sadad.constants.SadadCommercePaymentMethodConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

/**
 * @author Nader Jafari
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + CommerceAdminPortletKeys.COMMERCE_ADMIN,
		"mvc.command.name=editSadadCommercePaymentMethodConfigurationMVCActionCommand"
	},
	service = MVCActionCommand.class
)
public class EditSadadCommercePaymentMethodConfigurationMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

		if (cmd.equals(Constants.UPDATE)) {
			_updateCommercePaymentMethod(actionRequest);
		}
	}

	private void _updateCommercePaymentMethod(ActionRequest actionRequest)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Settings settings = _settingsFactory.getSettings(
			new GroupServiceSettingsLocator(
				themeDisplay.getScopeGroupId(),
					SadadCommercePaymentMethodConstants.SERVICE_NAME));

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		String merchantId = ParamUtil.getString(
			actionRequest, "settings--merchantId--");
		String merchantKey = ParamUtil.getString(
			actionRequest, "settings--merchantKey--");
		String terminalId = ParamUtil.getString(
			actionRequest, "settings--terminalId--");

        System.out.println("terminalId = " + terminalId);
        System.out.println("merchantKey = " + merchantKey);
        System.out.println("merchantId = " + merchantId);


		modifiableSettings.setValue("merchantId", merchantId);
		modifiableSettings.setValue("merchantKey", merchantKey);
		modifiableSettings.setValue("terminalId", terminalId);
		modifiableSettings.store();
	}

	@Reference
	private SettingsFactory _settingsFactory;

}