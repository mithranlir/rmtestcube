package rmlib.channel;

import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.IMessageChannel;
import com.qfs.source.IStoreChannelFactory;
import com.qfs.source.ITuplePublisher;
import com.qfs.source.impl.AutoCommitTuplePublisher;
import com.qfs.source.impl.POJOMessageChannelFactory;
import com.qfs.source.impl.TuplePublisher;
import com.qfs.store.IDatastoreSchemaMetadata;
import com.qfs.store.impl.Datastore;
import com.qfs.store.impl.StoreUtils;

import java.util.List;

public class ChannelCreationHelper {

    public static IMessageChannel<String, Object> createAndConfigurePOJOChannel(
            POJOMessageChannelFactory factory,
            Datastore datastore,
            String topicName,
            String storeName,
            boolean autoCommit,
            List<IColumnCalculator<Object>> calculatedColumns) {

        configurePOJOMessageChannelFactory(
                datastore.getSchemaMetadata(), factory, topicName, storeName, calculatedColumns);

        return createChannel(
                factory, datastore, topicName, storeName, autoCommit);
    }

    public static IMessageChannel<String, Object> createChannel(
            IStoreChannelFactory<String, Object> factory,
            Datastore datastore,
            String topicName,
            String storeName,
            boolean autoCommit) {

        if(autoCommit) {
            final ITuplePublisher<String> publisher =
                    new AutoCommitTuplePublisher<>(new TuplePublisher<String>(datastore, storeName));
            return factory.createChannel(topicName, storeName, publisher);
        }
        else {
            return factory.createChannel(topicName, storeName);
        }
    }

    public static POJOMessageChannelFactory configurePOJOMessageChannelFactory(
            IDatastoreSchemaMetadata datastoreSchemaMetadata,
            POJOMessageChannelFactory factory,
            String topicName,
            String storeName,
            List<IColumnCalculator<Object>> calculatedColumns) {

        factory.setSourceColumns(
                topicName,
                storeName,
                StoreUtils.getFields(datastoreSchemaMetadata, storeName));

        if(calculatedColumns!=null && !calculatedColumns.isEmpty()) {
            factory.setCalculatedColumns(topicName, storeName, calculatedColumns);
        }

        return factory;
    }
}
