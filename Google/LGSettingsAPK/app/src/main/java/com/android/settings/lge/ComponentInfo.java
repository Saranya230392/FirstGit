package com.android.settings.lge;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Basic implementation of {@link IComponentInfo}
 * 
 * @author sw.maeng
 * 
 */
public class ComponentInfo implements Parcelable {
    private static final String TOKEN_DELI = "/";
    protected int order;
    protected String packageName;
    protected String className;
    protected CharSequence label;

    protected ComponentInfo() {
    }

    public ComponentInfo(ComponentInfo info) {
        if (info != null) {
            this.packageName = info.packageName;
            this.className = info.className;
            this.order = info.order;
        }
    }

    public ComponentInfo(String packageName, String className, int order) {
        this.packageName = packageName;
        this.className = className;
        this.order = order;
    }

    public ComponentInfo(ComponentName compName, int order) {
        this(compName.getPackageName(), compName.getClassName(), order);
    }

    /**
     * Returns package name of app.
     * 
     * @return Package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns class name which can be Launchable.
     * 
     * @return class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns order of button
     * 
     * @return
     */
    public int getOrder() {
        return order;
    }

    /**
     * Set order of button
     * 
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public ComponentName getComponentName() {
        return new ComponentName(packageName, className);
    }

    private static Comparator<ComponentInfo> orderComparator = new Comparator<ComponentInfo>() {
        @Override
        public int compare(ComponentInfo lhs, ComponentInfo rhs) {
            return lhs.getOrder() - rhs.getOrder();
        }
    };

    /**
     * Returns comparator. Compares with order.
     * 
     * @return
     */
    public static Comparator<ComponentInfo> getOrderComparator() {
        return orderComparator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.className);
        dest.writeCharSequence(this.label);
        dest.writeInt(order);
    }

    /**
     * Returns whether this app is selected or not
     * 
     * @return
     */
    public boolean isSelected() {
        return false;
    }

    /**
     * Implementation of parcelable interface
     */
    public static final Parcelable.Creator<ComponentInfo> CREATOR = new Creator<ComponentInfo>() {
        @Override
        public ComponentInfo createFromParcel(Parcel source) {
            ComponentInfo obj = new ComponentInfo();
            obj.packageName = source.readString();
            obj.className = source.readString();
            obj.label = source.readCharSequence();
            obj.order = source.readInt();
            return obj;
        }

        @Override
        public ComponentInfo[] newArray(int size) {
            return new ComponentInfo[size];
        }
    };

    @Override
    public String toString() {
        return String.format("App pkg:%s, order:%d", packageName, order);
    }

    public String writeToString() {
        StringBuffer buf = new StringBuffer();
        buf.append(order).append(TOKEN_DELI);
        buf.append(packageName).append(TOKEN_DELI);
        buf.append(className);
        return buf.toString();
    }

    public static ComponentInfo parseFromString(String string) {
        ComponentInfo info;
        StringTokenizer st = new StringTokenizer(string, TOKEN_DELI);
        try {
            int order = Integer.parseInt(st.nextToken());
            String packageName = st.nextToken();
            String className = st.nextToken();
            info = new ComponentInfo(packageName, className, order);
        } catch (NoSuchElementException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
        return info;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result
                + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        ComponentInfo other = (ComponentInfo)obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!packageName.equals(other.packageName)) {
            return false;
        }
        return true;
    }

}
