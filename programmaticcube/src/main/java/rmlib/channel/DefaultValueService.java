package rmlib.channel;

import rmlib.cubebuilder.CubeBuilder;

public interface DefaultValueService {
    Object getDefaultValue(String fieldName, CubeBuilder.StoreFields storeFields);
}
