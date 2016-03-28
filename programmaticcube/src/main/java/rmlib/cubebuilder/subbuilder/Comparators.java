package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.ComparatorDescription;
import com.quartetfs.biz.pivot.definitions.impl.ComparatorOrderDescription;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.ordering.IComparator;
import com.quartetfs.fwk.ordering.impl.CustomComparator;
import com.quartetfs.fwk.types.IFactoryValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Comparators {

    public static ComparatorBuilder builder() {
        return new ComparatorBuilder();
    }

    public static class ComparatorBuilder extends AbstractComponentBuilder<ComparatorBuilder, ComparatorDescription> {

        private String pluginKey = null;
        private String orderName = null;
        private List<String> values = null;

        public ComparatorBuilder withPluginkey(String pluginKey) {
            this.pluginKey = pluginKey;
            return self();
        }

        public ComparatorBuilder withOrderName(String orderName) {
            this.orderName = orderName;
            return self();
        }

        public ComparatorBuilder withOrderValuesAsList(List<String> values) {
            this.values = values;
            return self();
        }

        public ComparatorBuilder withOrderValuesAsStr(String valuesListStr) {
            this.values = Arrays.asList(valuesListStr.split(","));
            return self();
        }

        protected ComparatorDescription doBuild() {

            if(pluginKey!=null) {

                IComparator comparator;
                final IFactoryValue factory = (IFactoryValue) Registry.getExtendedPlugin(IComparator.class).valueOf(new Object[]{pluginKey});
                if(factory == null) {
                    System.err.println("There is no comparator associated with plugin key " + pluginKey);
                    return null;
                }
                else {
                    try {
                        comparator = (IComparator)factory.create(new Object[0]);
                    } catch (RuntimeException exception) {
                        exception.printStackTrace();
                        comparator = null;
                    }
                }

                final ComparatorDescription comparatorDescription = new ComparatorDescription(pluginKey);

                if (comparator !=null && comparator instanceof CustomComparator) {

                    final ArrayList orders = new ArrayList();
                    if("firstObjects".equals(orderName) || "lastObjects".equals(orderName)) {
                        if (values != null && !values.isEmpty()) {
                            orders.add(new ComparatorOrderDescription(orderName, values));
                        }
                    }
                    comparatorDescription.setOrders(orders);
                }
                return comparatorDescription;
            }

            return null;
        }
    }

}
