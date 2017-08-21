/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.wso2.scim2.compliance.objects;

import org.wso2.charon3.core.attributes.Attribute;
import org.wso2.charon3.core.attributes.SimpleAttribute;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.objects.AbstractSCIMObject;

public class SCIMServiceProviderConfig extends AbstractSCIMObject {

   public boolean getPatchSupported() throws CharonException {
       Attribute patchAttribute  = getAttribute("patch");
       return ((SimpleAttribute)(patchAttribute.getSubAttribute("supported"))).getBooleanValue();
   }

    public boolean getSortSupported() throws CharonException {
        Attribute patchAttribute  = getAttribute("sort");
        return ((SimpleAttribute)(patchAttribute.getSubAttribute("supported"))).getBooleanValue();
    }

    public boolean getBulkSupported() throws CharonException {
        Attribute patchAttribute  = getAttribute("bulk");
        return ((SimpleAttribute)(patchAttribute.getSubAttribute("supported"))).getBooleanValue();
    }
}