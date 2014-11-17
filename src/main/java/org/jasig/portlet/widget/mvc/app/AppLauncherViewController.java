/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.widget.mvc.app;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.WindowStateException;

import org.apache.commons.lang.StringUtils;
import org.jasig.portlet.widget.service.IExpressionProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class AppLauncherViewController {

    private static final String WINDOW_STATE_DETACHED = "detached";
    private static final String DEFAULT_VIEW_NAME = "defaultViewName";

    private static final String JSP_ICON = "app/icon";
    private static final String JSP_DETACHED = "app/detached";
//    private static final String JSP_CONFIG = "app/config";

    private static final String PREFERENCE_ICON_SIZE_PIXELS = "AppLauncherPortletController.iconSizePixels";
    private static final String DEFAULT_ICON_SIZE_PIXELS = "200";

    private IExpressionProcessor expressionProcessor;


    @Autowired
    public void setExpressionProcessor(final IExpressionProcessor expressionProcessor) {
        this.expressionProcessor = expressionProcessor;
    }


    @RenderMapping()
    public String view(PortletRequest req) throws Exception {
        // As a precaution, clear the in-process config settings (if any)...
        AppLauncherConfigController.clearAppDefinitionInProgress(req);

        PortletPreferences preferences = req.getPreferences();
        String view = preferences.getValue(DEFAULT_VIEW_NAME, JSP_ICON);

        return WINDOW_STATE_DETACHED.equalsIgnoreCase(req.getWindowState().toString())
                ? JSP_DETACHED
                : view;
    }

    @ModelAttribute("iconSizePixels")
    public String getIconSizePixels(PortletRequest req) {
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValue(PREFERENCE_ICON_SIZE_PIXELS, DEFAULT_ICON_SIZE_PIXELS);
    }

    @ModelAttribute("appDefinition")
    public AppDefinition getAppDefinition(PortletRequest req) throws WindowStateException {
        return AppDefinition.fromPortletPreferences(req);
    }

    @ModelAttribute("appUrl")
    public String getAppUrl(PortletRequest req) throws WindowStateException {
        AppDefinition def = getAppDefinition(req);

        String configuredUrl = def.getAppUrl();
        String processedUrl = expressionProcessor.process(configuredUrl, req);

        return processedUrl;
    }
}
