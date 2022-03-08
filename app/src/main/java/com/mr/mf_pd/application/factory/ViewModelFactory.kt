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
import com.mr.mf_pd.application.view.check.ac.CheckACViewModel
import com.mr.mf_pd.application.view.check.ac.flight.ACFlightModelViewModel
import com.mr.mf_pd.application.view.check.ac.pulse.ACPulseModelViewModel
import com.mr.mf_pd.application.view.check.ac.setting.ACSettingViewModel
import com.mr.mf_pd.application.view.check.activity.CheckDataViewModel
import com.mr.mf_pd.application.view.check.hf.CheckHFViewModel
import com.mr.mf_pd.application.view.check.hf.setting.HFSettingViewModel
import com.mr.mf_pd.application.view.check.tev.CheckTEVViewModel
import com.mr.mf_pd.application.view.check.tev.setting.TEVSettingViewModel
import com.mr.mf_pd.application.view.check.uhf.CheckUHFViewModel
import com.mr.mf_pd.application.view.check.uhf.setting.UHFSettingViewModel
import com.mr.mf_pd.application.view.data.FileDataViewModel
import com.mr.mf_pd.application.view.fragment.continuity.ContinuityModelViewModel
import com.mr.mf_pd.application.view.fragment.phase.PhaseModelViewModel
import com.mr.mf_pd.application.view.fragment.real.RealModelViewModel
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
            isAssignableFrom(CheckUHFViewModel::class.java) ->
                CheckUHFViewModel(dataRepository,settingRepository)
            isAssignableFrom(RealModelViewModel::class.java) ->
                RealModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(PhaseModelViewModel::class.java) ->
                PhaseModelViewModel(dataRepository,filesRepository)
            isAssignableFrom(UHFSettingViewModel::class.java) ->
                UHFSettingViewModel(settingRepository)
            isAssignableFrom(CheckACViewModel::class.java) ->
                CheckACViewModel(dataRepository,settingRepository)

            isAssignableFrom(ACSettingViewModel::class.java) ->
                ACSettingViewModel(settingRepository)
            isAssignableFrom(ACFlightModelViewModel::class.java) ->
                ACFlightModelViewModel(dataRepository)
            isAssignableFrom(ACPulseModelViewModel::class.java) ->
                ACPulseModelViewModel(dataRepository)
            isAssignableFrom(HFSettingViewModel::class.java) ->
                HFSettingViewModel(settingRepository)
            isAssignableFrom(CheckHFViewModel::class.java) ->
                CheckHFViewModel(dataRepository,settingRepository)

            isAssignableFrom(CheckTEVViewModel::class.java) ->
                CheckTEVViewModel(dataRepository,settingRepository)
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
