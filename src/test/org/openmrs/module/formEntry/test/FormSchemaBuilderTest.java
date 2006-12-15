package test.org.openmrs.module.formEntry.test;

import org.openmrs.BaseTest;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formEntry.FormSchemaBuilder;

public class FormSchemaBuilderTest extends BaseTest {

	
	public void testClass() throws Exception {
		
		startup();
		
		Context.authenticate("admin", "test");
		
		Form form = Context.getFormService().getForm(17);
		String payload = new FormSchemaBuilder(form).getSchema();
		
		System.out.println(payload);
		
		shutdown();
	}
	
}