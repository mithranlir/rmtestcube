package rmlib.channel;

import com.qfs.msg.IMessage;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.IMessageChunk;
import rmlib.cubebuilder.CubeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelFeedHelper {

    public static void feedChannelWithMapList(List<Map<String, Object>> list,
                                              CubeBuilder.StoreFields storeFields,
                                              DefaultValueService defaultValueService,
                                              IMessageChannel<String, Object> channel) {
        final List<?> newList =
                completeMissingFieldsInMapList(list, storeFields, defaultValueService);
        feedChannel(newList, channel);
    }

    public static List<Map<String, Object>> completeMissingFieldsInMapList(
            List<Map<String, Object>> list,
            CubeBuilder.StoreFields storeFields,
            DefaultValueService defaultValueService) {

        if(storeFields == null || defaultValueService == null) {
            return list;
        }

        final List<Map<String, Object>> newList = new ArrayList<>();
        for(Object obj : list) {
            final Map newMap = new HashMap((Map)obj);
            for(String fieldName : storeFields.getFieldNames()) {
                if(!newMap.containsKey(fieldName)) {
                    final Object result = defaultValueService.getDefaultValue(fieldName, storeFields);
                    newMap.put(fieldName, result);
                }
            }
        }

        return newList;
    }

    public static void feedChannel(List<?> list, IMessageChannel<String, Object> channel) {
        int remaining = list.size();
        int batchSize = 100;
        while(remaining > 0) {
            final int batch = Math.min(remaining, batchSize);
            final IMessage<String, Object> message = channel.newMessage(channel.getTopic());
            final IMessageChunk<Object> chunk = message.newChunk();
            for(int i = 0; i < batch; i++) {
                int idx = list.size() - remaining + i;
                chunk.append(list.get(idx));
            }
            message.append(chunk);
            channel.send(message);
            remaining -= batch;
        }
    }

}
