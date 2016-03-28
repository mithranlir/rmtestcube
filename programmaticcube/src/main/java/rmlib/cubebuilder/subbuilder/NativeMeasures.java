package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.NativeMeasureDescription;
import com.quartetfs.fwk.format.IFormatter;

public class NativeMeasures {

    public static NativeMeasureDescriptionBuilder builder() {
        return new NativeMeasureDescriptionBuilder();
    }

    public static class NativeMeasureDescriptionBuilder extends AbstractComponentBuilder<NativeMeasureDescriptionBuilder, NativeMeasureDescription> {

        private String name = null;
        private String alias = null;
        private String folder = null;
        private IFormatter formatter = null;
        private String group = null;
        private Boolean visible = null;

        public NativeMeasureDescriptionBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public NativeMeasureDescriptionBuilder withAlias(String alias) {
            this.alias = alias;
            return self();
        }

        public NativeMeasureDescriptionBuilder withFolder(String folder) {
            this.folder = folder;
            return self();
        }

        public NativeMeasureDescriptionBuilder withFormatter(IFormatter formatter) {
            this.formatter = formatter;
            return self();
        }

        public NativeMeasureDescriptionBuilder withGroup(String group) {
            this.group = group;
            return self();
        }

        public NativeMeasureDescriptionBuilder withVisible(boolean visible) {
            this.visible = visible;
            return self();
        }

        protected NativeMeasureDescription doBuild() {

            final NativeMeasureDescription nativeMeasureDescription = new NativeMeasureDescription();

            nativeMeasureDescription.setName(name);

            if (alias != null) {
                nativeMeasureDescription.setAlias(alias);
            }

            if (folder != null) {
                nativeMeasureDescription.setFolder(folder);
            }

            if (formatter != null) {
                nativeMeasureDescription.setFormatter(formatter);
            }

            if (group != null) {
                nativeMeasureDescription.setGroup(group);
            }

            if (visible != null) {
                nativeMeasureDescription.setVisible(visible);
            }

            return nativeMeasureDescription;
        }
    }
}
