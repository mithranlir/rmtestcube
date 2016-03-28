package rmlib.transaction;

import com.qfs.store.impl.Datastore;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.qfs.store.transaction.IDatastoreSchemaTransactionInformation;
import com.qfs.store.transaction.ITransactionalWriter;
import com.quartetfs.fwk.QuartetRuntimeException;

public class TransactionHelper {

    public static void startTransaction(Datastore datastore) {
        final ITransactionalWriter transactionalWriter = datastore.getTransactionManager();
        try {
            transactionalWriter.asTransactionManager().startTransaction();
        } catch (DatastoreTransactionException e) {
            throw new QuartetRuntimeException("Could not start Trade-Risk transaction", e);
        }
    }

    public static IDatastoreSchemaTransactionInformation commitTransaction(Datastore datastore) {
        final ITransactionalWriter transactionalWriter = datastore.getTransactionManager();
        try {
            return transactionalWriter.asTransactionManager().commitTransaction();
        } catch (DatastoreTransactionException e) {
            try {
                transactionalWriter.asTransactionManager().rollbackTransaction();
            } catch (DatastoreTransactionException re) {
                throw new QuartetRuntimeException("The transaction rollback has failed.", re);
            }
            throw new QuartetRuntimeException("There was an error committing message", e);
        }
    }

}
