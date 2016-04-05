package rmlib.manager;


import com.qfs.store.IReadableDatastore;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IActivePivotSchema;
import com.quartetfs.biz.pivot.ICatalog;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.definitions.IActivePivotManagerDescription;
import com.quartetfs.fwk.AgentException;

import java.util.Map;
import java.util.Properties;

public class ActivePivotManagerWrapper implements IActivePivotManager {
    private static final long serialVersionUID = 1L;
    protected IActivePivotManager current;

    public ActivePivotManagerWrapper() {
        this(null);
    }

    public ActivePivotManagerWrapper(IActivePivotManager current) {
        this.current = current;
    }

    public void changeManager(IActivePivotManager newManager, boolean start) throws AgentException {
        if(this.current!=null) {
            this.current.stop();
        }
        this.current = newManager;
        if(start) {
            final Properties initProperties = this.current!=null ? this.current.getProperties() : null;
            this.current.init(initProperties);
            this.current.start();
        }
    }

    public void stop() throws AgentException {
        if(this.current != null && this.current.getStatus() != State.STOPPED) {
            this.current.stop();
        }
        this.current = null;
        for(int i = 0; i < 10; ++i) {
            System.gc();
        }
    }

    public String getType() {
        return this.current.getType();
    }

    public String getActivePivotVersion() {
        return this.current.getActivePivotVersion();
    }

    public Map<String, IActivePivotSchema> getSchemas() {
        return this.current.getSchemas();
    }

    public Map<String, ICatalog> getCatalogs() {
        return this.current.getCatalogs();
    }

    public Map<String, IMultiVersionActivePivot> getActivePivots() {
        return this.current.getActivePivots();
    }

    public IReadableDatastore getDatastore() {
        return this.current.getDatastore();
    }

    public IActivePivotManagerDescription getDescription() {
        return this.current.getDescription();
    }

    public void init(Properties props) throws AgentException {
        this.current.init(props);
    }

    public Properties getProperties() {
        return this.current.getProperties();
    }

    public State getStatus() {
        return this.current.getStatus();
    }

    public void start() throws AgentException {
        this.current.start();
    }

    public void pause() throws AgentException {
        this.current.pause();
    }

    public void resume() throws AgentException {
        this.current.resume();
    }
    public IActivePivotManager getActivePivotManager() {
        return current;
    }
}
