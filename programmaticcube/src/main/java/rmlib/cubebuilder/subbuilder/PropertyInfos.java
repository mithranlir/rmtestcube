package rmlib.cubebuilder.subbuilder;

import com.quartetfs.fwk.impl.PropertyInfo;

public class PropertyInfos {

    public static PropertyInfoBuilder builder() {
        return new PropertyInfoBuilder();
    }

    public static PropertyInfo expression(String name, String expression) {
        return new PropertyInfoBuilder().withName(name).withExpression(expression).build();
    }

    public static PropertyInfo plugin(String name, String pluginKey) {
        return new PropertyInfoBuilder().withName(name).withPluginKey(pluginKey).build();
    }

    public static class PropertyInfoBuilder extends AbstractComponentBuilder<PropertyInfoBuilder, PropertyInfo> {

        private String name = null;
        private String expression = null;
        private String pluginKey = null;

        public PropertyInfoBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public PropertyInfoBuilder withExpression(String expression)   {
            this.expression = expression;
            return self();
        }

        public PropertyInfoBuilder withPluginKey(String pluginKey)   {
            this.pluginKey = pluginKey;
            return self();
        }

        protected PropertyInfo doBuild() {

            final PropertyInfo propertyInfo = new PropertyInfo();
            propertyInfo.setName(name);

            if(expression!=null) {
                propertyInfo.setExpression(expression);
            }

            if(pluginKey!=null) {
                propertyInfo.setPluginKey(pluginKey);
            }

            return propertyInfo;
        }
    }
}
