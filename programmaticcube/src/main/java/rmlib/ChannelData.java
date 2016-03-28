package rmlib;

import com.qfs.msg.IMessageChannel;

import java.util.List;
import java.util.Map;

public class ChannelData {

    private IMessageChannel<String, Object> channel;
    private String storeName;
    private List<?> data;


    public ChannelData(IMessageChannel<String, Object> channel, String storeName, List<?> data) {
        this.channel = channel;
        this.data = data;
        this.storeName = storeName;
    }

    public IMessageChannel<String, Object> getChannel() {
        return channel;
    }

    public List<?> getData() {
        return data;
    }

    public List<Map<String, Object>> getDataAsMapList() {
        return (List<Map<String, Object>>) getData() ;
    }

    public String getStoreName() {
        return storeName;
    }
}
