package rmlib.cubebuilder.subbuilder;

import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.format.IFormatter;
import com.quartetfs.fwk.types.IFactoryValue;

public class Formatters {

    public static FormatterBuilder builder() {
        return new FormatterBuilder();
    }

    public static IFormatter formatter(String formatter) {
        return new FormatterBuilder().withFormatter(formatter).build();
    }

    public static class FormatterBuilder extends AbstractComponentBuilder<FormatterBuilder, IFormatter> {

        private String format = null;

        public FormatterBuilder withFormatter(String format) {
            this.format = format;
            return self();
        }

        protected IFormatter doBuild() {

            // taken from FormattersAdapter.FormatterDescription

            final String[] groups = format.split("\\[|\\]");
            final String plugin = groups[0];
            final String pattern = groups.length > 1?groups[1]:null;
            final IFactoryValue factory = (IFactoryValue) Registry.getExtendedPlugin(IFormatter.class).valueOf(new Object[]{plugin});
            if(factory == null) {
                System.err.println("There is no formatter associated with plugin key " + plugin);
                return null;
            }
            else {
                try {
                    IFormatter formatter;
                    if(pattern != null) {
                        formatter = (IFormatter)factory.create(new Object[]{pattern});
                    }
                    else {
                        formatter = (IFormatter)factory.create(new Object[0]);
                    }

                    return formatter;
                }
                catch (RuntimeException exception) {
                    exception.printStackTrace();
                    return null;
                }
            }
        }
    }
}
