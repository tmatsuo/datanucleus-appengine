/*
 * Copyright (C) 2010 Google Inc
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
package com.google.appengine.datanucleus.jdo;

import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.datanucleus.DatastoreManager;
import com.google.appengine.datanucleus.Utils;

import java.util.Map;

import javax.jdo.JDOHelper;

/**
 * @author Max Ross <max.ross@gmail.com>
 */
public class JDOTransactionOptionsTest extends JDOTestCase {

  public void testDefault() {
    DatastoreManager storeMgr = (DatastoreManager) getExecutionContext().getStoreManager();
    TransactionOptions txnOpts = storeMgr.getDefaultDatastoreTransactionOptions();
    assertFalse(txnOpts.isXG());
  }

  public void testIsXG_Props() {
    pm.close();
    pmf.close();
    Map<String, String> props = Utils.newHashMap();
    props.put("datanucleus.appengine.datastoreEnableXGTransactions", Boolean.TRUE.toString());
    pmf = JDOHelper.getPersistenceManagerFactory(props, getPersistenceManagerFactoryName().name());
    pm = pmf.getPersistenceManager();
    DatastoreManager storeMgr = (DatastoreManager) getExecutionContext().getStoreManager();
    TransactionOptions txnOpts = storeMgr.getDefaultDatastoreTransactionOptions();
    assertTrue(txnOpts.isXG());
  }

  public void testIsNotXG_Props() {
    pm.close();
    pmf.close();
    Map<String, String> props = Utils.newHashMap();
    props.put("datanucleus.appengine.datastoreEnableXGTransactions", Boolean.FALSE.toString());
    pmf = JDOHelper.getPersistenceManagerFactory(props, getPersistenceManagerFactoryName().name());
    pm = pmf.getPersistenceManager();
    DatastoreManager storeMgr = (DatastoreManager) getExecutionContext().getStoreManager();
    TransactionOptions txnOpts = storeMgr.getDefaultDatastoreTransactionOptions();
    assertFalse(txnOpts.isXG());
  }

  public void testIsXG_Config() {
    pm.close();
    pmf.close();
    pmf = JDOHelper.getPersistenceManagerFactory("allowXGTxns");
    pm = pmf.getPersistenceManager();
    DatastoreManager storeMgr = (DatastoreManager) getExecutionContext().getStoreManager();
    TransactionOptions txnOpts = storeMgr.getDefaultDatastoreTransactionOptions();
    assertTrue(txnOpts.isXG());
  }

  public void testIsNotXG_Config() {
    pm.close();
    pmf.close();
    pmf = JDOHelper.getPersistenceManagerFactory("disallowXGTxns");
    pm = pmf.getPersistenceManager();
    DatastoreManager storeMgr = (DatastoreManager) getExecutionContext().getStoreManager();
    TransactionOptions txnOpts = storeMgr.getDefaultDatastoreTransactionOptions();
    assertFalse(txnOpts.isXG());
  }
}
