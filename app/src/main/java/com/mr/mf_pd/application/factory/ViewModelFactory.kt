/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.mr.mf_pd.application.factory

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.repository.impl.UserRepository
import com.mr.mf_pd.application.view.check.flight.ACFlightModelViewModel
import com.mr.mf_pd.application.view.check.pulse.ACPulseModelViewModel
import com.mr.mf_pd.application.view.setting.ae.AESettingViewModel
import com.mr.mf_pd.application.view.check.CheckDataViewModel
import com.mr.mf_pd.application.view.setting.hf.HFSettingViewModel
import com.mr.mf_pd.application.view.setting.tev.TEVSettingViewModel
import com.mr.mf_pd.application.view.setting.uhf.UHFSettingViewModel
import com.mr.mf_pd.application.view.data.FileDataViewModel
import com.mr.mf_pd.application.view.check.continuity.ContinuityModelViewModel
import com.mr.mf_pd.application.view.check.phase.PhaseModelViewModel
import com.mr.mf_pd.application.view.check.real.RealModelViewModel
import com.mr.mf_pd.application.view.main.MainViewModel
import com.mr.mf_pd.application.view.setting.SettingViewModel
import com.mr.mf_pd.application.view.splash.SplashViewModel


/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val dataRepository: DataRepository,
    private val filesRepository: FilesRepository,
    private val settingRepository: SettingRepository,
    private val userRepository: UserRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(userRepository)
            isAssignableFrom(SplashViewModel::class.java) ->
                SplashViewModel(userRepository)
            isAssignableFrom(RealModelViewModel::class.java) ->
                RealModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(PhaseModelViewModel::class.java) ->
                PhaseModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(UHFSettingViewModel::class.java) ->
                UHFSettingViewModel(settingRepository)


            isAssignableFrom(AESettingViewModel::class.java) ->
                AESettingViewModel(settingRepository)
            isAssignableFrom(ACFlightModelViewModel::class.java) ->
                ACFlightModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(ACPulseModelViewModel::class.java) ->
                ACPulseModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(HFSettingViewModel::class.java) ->
                HFSettingViewModel(settingRepository)

            isAssignableFrom(TEVSettingViewModel::class.java) ->
                TEVSettingViewModel(settingRepository)
            isAssignableFrom(ContinuityModelViewModel::class.java) ->
                ContinuityModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(SettingViewModel::class.java) ->
                SettingViewModel(settingRepository)
            isAssignableFrom(CheckDataViewModel::class.java) ->
                CheckDataViewModel(dataRepository,settingRepository)
            isAssignableFrom(FileDataViewModel::class.java) ->
                FileDataViewModel(dataRepository,filesRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
