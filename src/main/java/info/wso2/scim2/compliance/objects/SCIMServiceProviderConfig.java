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
}