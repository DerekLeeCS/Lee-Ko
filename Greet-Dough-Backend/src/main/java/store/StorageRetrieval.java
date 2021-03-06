package store;

import java.io.Serializable;
import java.util.HashMap;

public abstract class StorageRetrieval<T> implements Serializable {

    protected HashMap<Integer, T> items;

    private static final long serialVersionUID = 1L;

    protected StorageRetrieval() {
        this.items = new HashMap<>();
    }

    protected T get( int ID ) {
        return this.items.get(ID);
    }

    protected HashMap<Integer, T> getItems() {
        return this.items;
    }

    protected void add( int key, T newItem ) {
        this.items.put( key, newItem );
    }

    protected boolean has( int ID ) {
        return this.items.containsKey(ID);
    }

    // Returns:
    //      1   if successful
    //      0   if unsuccessful
    protected boolean delete( int ID ) {
        return ( this.items.remove(ID) != null );
    }

}
