package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.definitions.IComparatorDescription;
import com.quartetfs.biz.pivot.definitions.impl.AxisLevelDescription;
import com.quartetfs.fwk.IPropertyInfo;
import com.quartetfs.fwk.format.IFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AxisLevels {

    public static AxisLevelDescriptionBuilder builder() {
        return new AxisLevelDescriptionBuilder();
    }

    public static AxisLevelDescriptionBuilder level(String levelName) {
        return new AxisLevelDescriptionBuilder().withLevelName(levelName);
    }

    public static AxisLevelDescriptionBuilder level(String levelName, ILevelInfo.LevelType levelType) {
        return new AxisLevelDescriptionBuilder().withLevelName(levelName).withLevelType(levelType);
    }

    public static class AxisLevelDescriptionBuilder extends AbstractComponentBuilder<AxisLevelDescriptionBuilder, AxisLevelDescription> {

        private String levelName = null;
        private String propertyName= null;
        private Properties properties = null;
        private ILevelInfo.LevelType levelType = null;
        private List<IPropertyInfo> memberProperties = null;
        private IComparatorDescription comparator = null;
        private IFormatter formatter = null;

        public AxisLevelDescriptionBuilder withLevelName(String levelName) {
            this.levelName = levelName;
            return self();
        }

        public AxisLevelDescriptionBuilder withPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return self();
        }

        public AxisLevelDescriptionBuilder withProperty(String key, String value) {
            if(this.properties==null) {
                this.properties = new Properties();
            }
            this.properties.put(key, value);
            return self();
        }

        public AxisLevelDescriptionBuilder withLevelType(ILevelInfo.LevelType levelType) {
            this.levelType = levelType;
            return self();
        }

        public AxisLevelDescriptionBuilder withMemberProperty(IPropertyInfo property) {
            if(this.memberProperties==null) {
                this.memberProperties = new ArrayList<>();
            }
            if(property!=null) {
                this.memberProperties.add(property);
            }
            return self();
        }

        public AxisLevelDescriptionBuilder withComparator(IComparatorDescription comparator) {
            this.comparator = comparator;
            return self();
        }

        public AxisLevelDescriptionBuilder withFormatter(IFormatter formatter) {
            this.formatter = formatter;
            return self();
        }

        protected AxisLevelDescription doBuild() {

            final AxisLevelDescription axisLevelDescription = new AxisLevelDescription();

            axisLevelDescription.setLevelName(levelName);

            if(propertyName!=null) {
                axisLevelDescription.setPropertyName(propertyName);
            }

            if(levelType!=null) {
                axisLevelDescription.setLevelType(levelType);
            }

            if(memberProperties!=null) {
                axisLevelDescription.setMemberProperties(memberProperties);
            }

            if(comparator!=null) {
                axisLevelDescription.setComparator(comparator);
            }

            if(formatter!=null) {
                axisLevelDescription.setFormatter(formatter);
            }

            return axisLevelDescription;
        }
    }
}
