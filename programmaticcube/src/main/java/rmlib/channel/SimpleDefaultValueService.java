package rmlib.channel;

import com.qfs.store.Types;
import rmlib.cubebuilder.CubeBuilder;

public class SimpleDefaultValueService implements DefaultValueService {

    @Override
    public Object getDefaultValue(String fieldName, CubeBuilder.StoreFields storeFields) {
        Object result = getCustomDefaultValue(fieldName);
        if(result!=null) {
            return result;
        }
        return getDefaultValueForBasicType(storeFields.getFieldTypeMap().get(fieldName));
    }

    // to override to manage custom default value...
    protected Object getCustomDefaultValue(String fieldName) {
        return null;
    }

    private Object getDefaultValueForBasicType(int type) {
        Object result;
        if(type == Types.TYPE_STRING) {
            result = "NO_VALUE";
        }
        else if(type == Types.TYPE_BOOLEAN) {
            result = false;
        }
        else if(type == Types.TYPE_INT) {
            result = Integer.valueOf(0);
        }
        else if(type == Types.TYPE_LONG) {
            result = Long.valueOf(0);
        }
        else if(type == Types.TYPE_FLOAT) {
            result = Float.valueOf(0);
        }
        else if(type == Types.TYPE_DOUBLE) {
            result = Double.valueOf(0);
        }
        else {
            result = null;
        }
        return result;
    }
}
