package com.android.settings.remote;

import android.app.Activity;
import android.app.Fragment;
import android.app.LGFragment;
import android.app.LGListFragment;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.LGPreferenceFragment;

import java.lang.reflect.Constructor;

public class RemoteFragment {
    public static Fragment instantiate(final Context context, String packageName,
            String fragmentName, Bundle bundle) {
        Constructor<? extends Fragment> constructor = null;
        Class<? extends Fragment> clazz = null;
        Fragment fragment = null;

        try {
            Context remote_context = null;

            try {
                int flags = (Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
                remote_context = context.createPackageContext(packageName,
                        flags);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

	if (remote_context != null) {
            		clazz = remote_context.getClassLoader().loadClass(fragmentName)
	                    .asSubclass(Fragment.class);

	            	Class args[] = new Class[] {};
            		constructor = clazz.getConstructor(args);

            		fragment = constructor.newInstance();
            }

            if (fragment != null) {
                if (fragment instanceof LGFragment) {
                    ((LGFragment)fragment).setPackageContext(context, packageName, bundle);
                } else if (fragment instanceof LGPreferenceFragment) {
                    ((LGPreferenceFragment)fragment).setPackageContext(context, packageName,
                            bundle);
                } else if (fragment instanceof LGListFragment) {
                    ((LGListFragment)fragment).setPackageContext(context, packageName,
                            bundle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fragment;
    }
}