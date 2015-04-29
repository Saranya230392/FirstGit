/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.service.trust.TrustAgentService;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.android.settings.R;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SettingsSwitchPreference;

public class TrustAgentSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    static final String TAG = "TrustAgentSettings";
    private static final String SERVICE_INTERFACE = TrustAgentService.SERVICE_INTERFACE;
    private ArrayMap<ComponentName, AgentInfo> mAvailableAgents;
    private final ArraySet<ComponentName> mActiveAgents = new ArraySet<ComponentName>();
    private LockPatternUtils mLockPatternUtils;
    private ListView mListView;
    private LinearLayout mEmptyViewLayout;
    private TextView mTextView;
    private int mCount = 0;

    public static final class AgentInfo {
        CharSequence label;
        ComponentName component; // service that implements ITrustAgent
        SettingsSwitchPreference preference;
        public Drawable icon;

        @Override
        public boolean equals(Object other) {
            if (other instanceof AgentInfo) {
                return component.equals(((AgentInfo)other).component);
            }
            return true;
        }

        public int compareTo(AgentInfo other) {
            return component.compareTo(other.component);
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.trust_agent_settings);
        updateAgents();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trust_agent_settings_empty, null);
        mEmptyViewLayout = (LinearLayout)view.findViewById(R.id.emptyView);
        mTextView = (TextView)view.findViewById(R.id.empty);
        mListView = (ListView)view.findViewById(android.R.id.list);
        if (mCount > 0) {
            mEmptyViewLayout.setVisibility(View.GONE);
            mTextView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            mEmptyViewLayout.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        return view;
    }

    public void onResume() {
        super.onResume();
    };

    private void updateAgents() {
        final Context context = getActivity();
        if (mAvailableAgents == null) {
            mAvailableAgents = findAvailableTrustAgents();
        }
        if (mLockPatternUtils == null) {
            mLockPatternUtils = new LockPatternUtils(getActivity());
        }
        loadActiveAgents();
        PreferenceGroup category =
                (PreferenceGroup)getPreferenceScreen().findPreference("trust_agents");
        category.removeAll();
        mCount = mAvailableAgents.size();
        for (int i = 0; i < mCount; i++) {
            AgentInfo agent = mAvailableAgents.valueAt(i);
            final SettingsSwitchPreference preference = new SettingsSwitchPreference(context);
            preference.setSwitchTextOn("");
            preference.setSwitchTextOff("");
            preference.setDivider(false);
            agent.preference = preference;
            preference.setPersistent(false);
            preference.setTitle(agent.label);
            preference.setIcon(agent.icon);
            preference.setPersistent(false);
            preference.setOnPreferenceChangeListener(this);
            preference.setChecked(mActiveAgents.contains(agent.component));
            category.addPreference(agent.preference);
        }
    }

    private void loadActiveAgents() {
        List<ComponentName> activeTrustAgents = mLockPatternUtils.getEnabledTrustAgents();
        if (activeTrustAgents != null) {
            mActiveAgents.addAll(activeTrustAgents);
        }
    }

    private void saveActiveAgents() {
        mLockPatternUtils.setEnabledTrustAgents(mActiveAgents);
    }

    ArrayMap<ComponentName, AgentInfo> findAvailableTrustAgents() {
        PackageManager pm = getActivity().getPackageManager();
        Intent trustAgentIntent = new Intent(SERVICE_INTERFACE);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(trustAgentIntent,
                PackageManager.GET_META_DATA);

        ArrayMap<ComponentName, AgentInfo> agents = new ArrayMap<ComponentName, AgentInfo>();
        final int count = resolveInfos.size();
        agents.ensureCapacity(count);
        for (int i = 0; i < count; i++ ) {
            ResolveInfo resolveInfo = resolveInfos.get(i);
            if (resolveInfo.serviceInfo == null) { continue; }
            if (!TrustAgentUtils.checkProvidePermission(resolveInfo, pm)) { continue; }
            ComponentName name = TrustAgentUtils.getComponentName(resolveInfo);
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.label = resolveInfo.loadLabel(pm);
            agentInfo.icon = resolveInfo.loadIcon(pm);
            agentInfo.component = name;
            agents.put(name, agentInfo);
        }
        return agents;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SettingsSwitchPreference) {
            final int count = mAvailableAgents.size();
            for (int i = 0; i < count; i++) {
                AgentInfo agent = mAvailableAgents.valueAt(i);
                if (agent.preference == preference) {
                    if ((Boolean)newValue) {
                        if (!mActiveAgents.contains(agent.component)) {
                            mActiveAgents.add(agent.component);
                        }
                    } else {
                        mActiveAgents.remove(agent.component);
                    }
                    saveActiveAgents();
                    return true;
                }
            }
        }
        return false;
    }

}
