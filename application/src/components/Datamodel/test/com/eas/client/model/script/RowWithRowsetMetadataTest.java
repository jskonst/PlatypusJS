/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.model.script;

import com.eas.client.DatabasesClient;
import com.eas.client.DatabasesClientWithResource;
import com.eas.client.model.application.ApplicationDbEntity;
import com.eas.client.model.application.ApplicationDbModel;
import com.eas.client.model.BaseTest;
import com.eas.script.ScriptUtils;
import org.junit.Test;

/**
 *
 * @author mg
 */
public class RowWithRowsetMetadataTest extends BaseTest {

    private static final String SCRIPT_TEST_SOURCE
            = "var loc = entity1.createLocator(entity1.schema.NAME);\n"
            + "loc.find('building1');\n"
            + "java.lang.System.out.println('loc.size: '+loc.size);"
            + "var lRow = loc.getRow(0);\n"
            + "var v = lRow.NAME;\n"
            + "if(v == undefined) throw 'variable v must have some value';"
            + "lRow.NAME = 'newName';\n"
            + "var cO = lRow.getColumnObject(2);\n"
            + "if(cO != 'newName') throw 'column object with col index equals to 2 already must be updated!';\n"
            + "if(v == 'newName') throw 'old value of column object with col index must remain, bu t it is not!';";

    @Test
    public void fieldsAccessTest() throws Exception {
        System.out.println("fieldsAccessTest");
        String entityName = "entity1";
        try (DatabasesClientWithResource resource = BaseTest.initDevelopTestClient()) {
            final DatabasesClient client = resource.getClient();
            ApplicationDbModel model = new ApplicationDbModel(client);
            final ApplicationDbEntity entity11 = model.newGenericEntity();
            entity11.setQueryId("128015347915605");
            model.addEntity(entity11);
            entity11.setName(entityName);
            ContextFactory cf = ContextFactory.getGlobal();
            Context cx = cf.enterContext();
            try {
                ScriptableObject scope = ScriptUtils.getScope();
                model.requery();
                model.setScriptThis(scope);
                // let's compile test script
                Script script = cx.compileString(SCRIPT_TEST_SOURCE, "rowWithRowsetFieldsTest", 0, null);
                script.exec(cx, scope);
            } finally {
                Context.exit();
            }
        }
    }
}
