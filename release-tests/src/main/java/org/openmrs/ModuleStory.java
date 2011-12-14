package org.openmrs;

import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

public class ModuleStory  {
    public static void main(String args[]) throws IOException, InterruptedException {
        if(!skipDatabaseSetupPage()){
        Runtime runtime = Runtime.getRuntime();
        runtime.exec("curl http://localhost:8080/openmrs/AUTO_RUN_OPENMRS?local=en&remember=true");
        Thread.sleep(1000*60*10);  //Waiting 10 minutes for installation to complete
        }
    }

    /**
     * Utility method that checks if there is a runtime properties file containing database
     * connection credentials
     *
     * @return
     */
    private static boolean skipDatabaseSetupPage() {
        Properties props = OpenmrsUtil.getRuntimeProperties(WebConstants.WEBAPP_NAME);
        return (props != null && StringUtils.hasText(props.getProperty("connection.url"))
                && StringUtils.hasText(props.getProperty("connection.username")) && StringUtils.hasText(props
                .getProperty("connection.password")));
    }


}