package com.android.settings.remote;

import java.util.ArrayList;

public class MultiDexClassLoader extends ClassLoader {
    private ArrayList<ClassLoader> mClassLoaders;

    public MultiDexClassLoader(ArrayList<ClassLoader> classLoaders) {
        super(ClassLoader.getSystemClassLoader());
        mClassLoaders = classLoaders;
    }

    public void setClassLoaders(ArrayList<ClassLoader> classLoaders) {
        mClassLoaders = null;
        mClassLoaders = classLoaders;
    }

    @Override
    public Class<?> findClass(String className) throws ClassNotFoundException {

        Class<?> clazz = null;
        for (ClassLoader classLoader : mClassLoaders) {
            try {
                clazz = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                continue;
            }
            if (clazz != null) {
                return clazz;
            }
        }

        throw new ClassNotFoundException(className + " in loader " + this);
    }

    public void clear() {
        if (mClassLoaders != null) {
            mClassLoaders.clear();
            mClassLoaders = null;
        }
    }
}
