/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.handler.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.handler.ReadOnlyTemplateHandler;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT_OFFSET;
import static org.wso2.carbon.identity.template.mgt.internal.TemplateManagerDataHolder.getFileBasedTemplates;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.handleClientException;

/**
 * File based template handler implementation.
 */
public class FileBasedTemplateHandler implements ReadOnlyTemplateHandler {

    private static final Log log = LogFactory.getLog(FileBasedTemplateHandler.class);
    private static final Integer DEFAULT_SEARCH_LIMIT = 100;

    @Override
    public Template getTemplateById(String templateId) throws TemplateManagementException {

        return getFileBasedTemplates().get(templateId);
    }

    @Override
    public List<TemplateInfo> listTemplates(Integer limit, Integer offset) throws TemplateManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = DEFAULT_SEARCH_LIMIT;
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined in the request, default to: " + limit);
            }
        }

        List<TemplateInfo> templateInfo = new ArrayList<>();
        getFileBasedTemplates().entrySet().stream()
                .skip(offset)
                .limit(limit)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()).
                forEach(entry -> {
                    TemplateInfo templateInfoObj = new TemplateInfo(Integer.parseInt(entry.getTemplateId()),
                            CarbonContext.getThreadLocalCarbonContext().getTenantId(), entry.getTemplateName());
                    templateInfo.add(templateInfoObj);
                });

        return templateInfo;
    }

    @Override
    public List<Template> listTemplates(String templateType, Integer limit, Integer offset)
            throws TemplateManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = DEFAULT_SEARCH_LIMIT;
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined in the request, default to: " + limit);
            }
        }

        return getFileBasedTemplates().entrySet().stream()
                .filter(entry -> StringUtils.equals(entry.getValue().getTemplateType().toString(), (templateType)))
                .skip(offset)
                .limit(limit)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * This method is used to validate the pagination parameters.
     *
     * @param limit  Limits the number of templates listed on a page.
     * @param offset Specifies the starting point for the templates to be displayed.
     * @throws TemplateManagementException Consent Management Exception.
     */
    private void validatePaginationParameters(Integer limit, Integer offset) throws TemplateManagementClientException {

        if (limit < 0 || offset < 0) {
            throw handleClientException(ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT_OFFSET, null);
        }
    }
}
