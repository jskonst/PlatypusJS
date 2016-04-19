/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.client.application;

import org.junit.Test;

/**
 *
 * @author mg
 */
public abstract class WithServerTests extends ScriptedTests {

    @Test
    public void select_stateless_test() throws InterruptedException {
        start("select_stateless_test", 20000L);
    }

    @Test
    public void easHRValidatorTest() throws InterruptedException {
        start("EasHRValidatorTest", 10000L);
    }

    @Test
    public void append_test() throws InterruptedException {
        start("append_test", 10000L);
    }

    @Test
    public void ambigous_changes_semi_writable() throws InterruptedException {
        start("ambigous_changes_semi_writable", 10000L);
    }

    @Test
    public void ambigous_changes() throws InterruptedException {
        start("ambigous_changes", 20000L);
    }

    @Test
    public void extra_fields_insert_update() throws InterruptedException {
        start("extra_fields_insert_update", 10000L);
    }

    @Test
    public void modelModyfiedTestClient() throws InterruptedException {
        start("ModelModyfiedTestClient", 10000L);
    }

    @Test
    public void sqlUpdateTestClient() throws InterruptedException {
        start("SqlUpdateTestClient", 10000L);
    }

    @Test
    public void dependenciesTest() throws InterruptedException {
        start("DependenciesTest", 20000L);
    }

    @Test
    public void parallelRequireTest() throws InterruptedException {
        start("ParallelRequireTest", 10000L);
    }

    @Test
    public void createEntityTestClient() throws InterruptedException {
        start("CreateEntityTestClient", 10000L);
    }

    @Test
    public void loadEntityTestClient() throws InterruptedException {
        start("LoadEntityTestClient", 10000L);
    }

    @Test
    public void modelAPI() throws InterruptedException {
        start("ModelAPI", 50000L);
    }

    @Test
    public void multiSourceTest() throws InterruptedException {
        start("MultiSourceTest", 10000L);
    }

    @Test
    public void multiSourceWithErrorTest() throws InterruptedException {
        start("MultiSourceWithErrorTest", 10000L);
    }

    @Test
    public void orm_Relations_Test() throws InterruptedException {
        start("ORM_Relations_Test", 20000L);
    }

    @Test
    public void orm_properties_names_calc() throws InterruptedException {
        start("ORM_properties_names_calc", 10000L);
    }

    @Test
    public void testReportClient() throws InterruptedException {
        start("TestReportClient", 10000L);
    }

    @Test
    public void iconLoadTest() throws InterruptedException {
        start("IconLoadTest", 10000L);
    }

    @Test
    public void resourceLoadTest() throws InterruptedException {
        start("ResourceLoadTest", 10000L);
    }

    @Test
    public void storedProcedureTestClient() throws InterruptedException {
        start("StoredProcedureTestClient", 10000_00000L);
    }

    @Test
    public void secureServerModulesClient() throws InterruptedException {
        start("SecureServerModulesClient", 10000L);
    }

    @Test
    public void secureDataSourcesTest() throws InterruptedException {
        start("SecureDataSourcesTest", 10000L);
    }

    @Test
    public void syncServerModulesTest() throws InterruptedException {
        start("SyncServerModulesTest", 10000L);
    }

    @Test
    public void asyncServerModulesTest() throws InterruptedException {
        start("AsyncServerModulesTest", 10000L);
    }

    @Test
    public void principalTestClient() throws InterruptedException {
        start("PrincipalTestClient", 10000L);
    }

    @Test
    public void accounterClient() throws InterruptedException {
        start("AccounterClient", 10000L);
    }

    @Test
    public void invokeLaterDelayedClient() throws InterruptedException {
        start("InvokeLaterDelayedClient", 10000L);
    }

    @Test
    public void invokeLaterDelayedTest() throws InterruptedException {
        start("InvokeLaterDelayedTest", 10000L);
    }

    @Test
    public void errorsTestClient() throws InterruptedException {
        start("ErrorsTestClient", 10000L);
    }

}
