/**********************************************************************
Copyright (c) 2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**********************************************************************/
package com.google.appengine.datanucleus;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import org.datanucleus.util.NucleusLogger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * Extension to {@link EmulatedXAResource} that manages a transaction against the datastore.
 * Currently only supports a single, non-distributed transaction.  Instances of this class are
 * instantiated and used when the datasource has been configured as "autoCreateTransaction" or the user is
 * explicitly doing transaction management.
 *
 * @author Erick Armbrust <earmbrust@google.com>
 * @author Max Ross <maxr@google.com>
 */
class DatastoreXAResource extends EmulatedXAResource {

  /** The current datastore transaction. */
  private DatastoreTransaction currentTxn;
  private final TransactionOptions txnOpts;

  public DatastoreXAResource(DatastoreService datastoreService, TransactionOptions txnOpts) {
    super(datastoreService);
    this.txnOpts = txnOpts;
  }

  @Override
  DatastoreTransaction getCurrentTransaction() {
    return currentTxn;
  }

  @Override
  public void start(Xid xid, int flags) throws XAException {
    super.start(xid, flags);
    if (currentTxn == null) {
      // No currentTxn, and DatastoreService will have been created by DatastoreConnectionFactoryImpl, so call beginTxn
      Transaction datastoreTxn = datastoreService.beginTransaction(txnOpts);
      currentTxn = new DatastoreTransaction(datastoreTxn);
      if (NucleusLogger.TRANSACTION.isDebugEnabled()) {
        NucleusLogger.TRANSACTION.debug(
            LOCALISER.msg("AppEngine.Transaction.Started", currentTxn.getInnerTxn().getId()));
      }
    } else {
      throw new XAException(LOCALISER.msg("AppEngine.Transaction.AlreadyStarted"));
    }
  }

  @Override
  public void commit(Xid xid, boolean onePhase) throws XAException {
    super.commit(xid, onePhase);
    if (currentTxn != null) {
      currentTxn.commit();
      if (NucleusLogger.TRANSACTION.isDebugEnabled()) {
        NucleusLogger.TRANSACTION.debug(
            LOCALISER.msg("AppEngine.Transaction.Committed", currentTxn.getInnerTxn().getId()));
      }
      currentTxn = null;
    } else {
      throw new XAException(LOCALISER.msg("AppEngine.Transaction.CommitInvalid"));
    }
  }

  @Override
  public void rollback(Xid xid) throws XAException {
    super.rollback(xid);
    if (currentTxn != null) {
      currentTxn.rollback();
      if (NucleusLogger.TRANSACTION.isDebugEnabled()) {
        NucleusLogger.TRANSACTION.debug(
            LOCALISER.msg("AppEngine.Transaction.RolledBack", currentTxn.getInnerTxn().getId()));
      }
      currentTxn = null;
    } else {
      throw new XAException(LOCALISER.msg("AppEngine.Transaction.RollbackInvalid"));
    }
  }
}
