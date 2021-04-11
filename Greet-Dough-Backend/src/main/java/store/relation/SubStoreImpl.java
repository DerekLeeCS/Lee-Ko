package store.relation;

import store.model.SubStore;

import java.util.ArrayList;

// Store subscriptions as ArrayList or HashMap?
// A single user's subscriptions probably won't be so large that a linear search will take very long
public class SubStoreImpl extends Relation implements SubStore {

    public SubStoreImpl() {
        super();
    }

    @Override
    public ArrayList<Integer> getSubscriptions( int uid ) {
        return super.get(uid);
    }

    @Override
    public void addSubscription( int curUser, int targetUser ) {
        super.add( curUser, targetUser );
    }

    // Removes targetUser from curUser's subscriptions
    @Override
    public void removeSubscription( int curUser, int targetUser ) {
        super.remove( curUser, targetUser );
    }

    // Deletes user along with all subscriptions
    @Override
    public boolean deleteUser( int uid ) {
        return super.delete(uid);
    }

}
