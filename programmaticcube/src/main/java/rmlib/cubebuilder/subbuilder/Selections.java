package rmlib.cubebuilder.subbuilder;

import com.qfs.store.selection.ISelectionField;
import com.qfs.store.selection.impl.SelectionField;
import com.quartetfs.biz.pivot.definitions.ISelectionFieldsDescription;
import com.quartetfs.biz.pivot.definitions.impl.SelectionDescription;
import com.quartetfs.biz.pivot.definitions.impl.SelectionFieldsDescription;

import java.util.ArrayList;
import java.util.List;

public class Selections {

    public static SelectionDescriptionBuilder builder() {
        return new SelectionDescriptionBuilder();
    }

    public static class SelectionDescriptionBuilder extends AbstractComponentBuilder<SelectionDescriptionBuilder, SelectionDescription> {

        private final List<ISelectionField> selectionFieldList = new ArrayList<>();
        private String baseStoreName;

        public SelectionDescriptionBuilder withBaseStore(String baseStoreName) {
            this.baseStoreName = baseStoreName;
            return self();
        }

        public SelectionDescriptionBuilder withSelection(String name, String expression) {
            selectionFieldList.add(new SelectionField(name, expression));
            return self();
        }

        public SelectionDescriptionBuilder withSelection(String name) {
            selectionFieldList.add(new SelectionField(name));
            return self();
        }

        protected SelectionDescription doBuild() {

            final ISelectionFieldsDescription selectionFieldsDescription = new SelectionFieldsDescription();
            selectionFieldsDescription.addValues(selectionFieldList);

            final SelectionDescription selectionDescription = new SelectionDescription();
            selectionDescription.setBaseStore(baseStoreName);
            selectionDescription.setSelectionFields(selectionFieldsDescription);

            return selectionDescription;
        }

    }

}
