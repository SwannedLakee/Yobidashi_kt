/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.toastkid.lib.night.DisplayMode
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.CompoundDrawableColorApplier
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingOtherBinding
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.settings.ClearSettingConfirmDialogFragment

/**
 * @author toastkidjp
 */
class OtherSettingFragment : Fragment() {

    /**
     * View Data Binding object.
     */
    private lateinit var binding: FragmentSettingOtherBinding

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private val intentFactory = SettingsIntentFactory()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        val activityContext = context
                ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.fragment = this

        binding.startUpItems.startUpSelector.setOnCheckedChangeListener { _, checkedId ->
            preferenceApplier.startUp = StartUp.findById(checkedId).name
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.startUpItems.startUpSelector.let {
            it.check(StartUp.findByName(preferenceApplier.startUp).radioButtonId)
            it.jumpDrawablesToCurrentState()
        }
        binding.wifiOnlyCheck.let {
            it.isChecked = preferenceApplier.wifiOnly
            it.jumpDrawablesToCurrentState()
        }

        val color =
                if (DisplayMode(resources.configuration).isNightMode()) preferenceApplier.fontColor
                else preferenceApplier.color
        CompoundDrawableColorApplier().invoke(
                color,
                binding.settingsDevice,
                binding.startUpItems.textStartUpTab,
                binding.settingsAllApps,
                binding.settingsDateAndTime,
                binding.settingsDisplay,
                binding.settingsWifi,
                binding.settingsWireless,
                binding.clearSettings
        )
    }

    /**
     * Switch Wi-Fi only mode.
     */
    fun switchWifiOnly() {
        val newState = !preferenceApplier.wifiOnly
        preferenceApplier.wifiOnly = newState
        binding.wifiOnlyCheck.isChecked = newState
    }


    /**
     * Clear all settings.
     */
    fun clearSettings() {
        ClearSettingConfirmDialogFragment().show(
                parentFragmentManager,
                ClearSettingConfirmDialogFragment::class.java.canonicalName
        )
    }

    /**
     * Call device settings.
     */
    fun deviceSetting() {
        startActivity(intentFactory.makeLaunch())
    }

    /**
     * Call Wi-Fi settings.
     */
    fun wifi() {
        startActivity(intentFactory.wifi())
    }

    /**
     * Call Wireless settings.
     */
    fun wireless() {
        startActivity(intentFactory.wireless())
    }

    /**
     * Call Date-and-Time settings.
     */
    fun dateAndTime() {
        startActivity(intentFactory.dateAndTime())
    }

    /**
     * Call display settings.
     */
    fun display() {
        startActivity(intentFactory.display())
    }

    /**
     * Call all app settings.
     */
    fun allApps() {
        startActivity(intentFactory.allApps())
    }

    companion object : TitleIdSupplier {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_setting_other

        @StringRes
        override fun titleId() = R.string.subhead_others

    }
}