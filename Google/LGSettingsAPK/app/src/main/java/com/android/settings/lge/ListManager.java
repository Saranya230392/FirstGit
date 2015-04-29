package com.android.settings.lge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class ListManager<K> {

    List<K> list;

    ListManager() {
        list = new ArrayList<K>();
    }

    /**
     * Create list
     * 
     * @return
     */
    public abstract List<K> createList(Set<String> scope);

    /**
     * Update list
     * 
     * @return
     */
    public abstract List<K> updateList(Set<String> scope);

    /**
     * Update list items
     * 
     * @return
     */
    public List<K> updateItems() {
        return list;
    }

    /**
     * Clear list.
     */
    public void clearList() {
        list.clear();
    }

    /**
     * Return size of list.
     */
    public int size() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

}
