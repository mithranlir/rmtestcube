package rmlib.cubebuilder.subbuilder;

public abstract class AbstractComponentBuilder<T,U> implements ComponentBuilder<U> {
    protected T self() {
        return (T) this;
    }
    protected abstract U doBuild();
    public U build() {
        return doBuild();
    }
}
