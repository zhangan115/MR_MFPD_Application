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
import com.mr.mf_pd.application.view.ac.CheckACViewModel
import com.mr.mf_pd.application.view.tev.continuity.TEVContinuityModelViewModel
import com.mr.mf_pd.application.view.ac.flight.ACFlightModelViewModel
import com.mr.mf_pd.application.view.ac.phase.ACPhaseModelViewModel
import com.mr.mf_pd.application.view.tev.phase.TEVPhaseModelViewModel
import com.mr.mf_pd.application.view.ac.pulse.ACPulseModelViewModel
import com.mr.mf_pd.application.view.ac.real.ACRealModelViewModel
import com.mr.mf_pd.application.view.tev.real.TEVRealModelViewModel
import com.mr.mf_pd.application.view.ac.setting.ACSettingViewModel
import com.mr.mf_pd.application.view.hf.CheckHFViewModel
import com.mr.mf_pd.application.view.hf.phase.HFPhaseModelViewModel
import com.mr.mf_pd.application.view.hf.real.HFRealModelViewModel
import com.mr.mf_pd.application.view.hf.setting.HFSettingViewModel
import com.mr.mf_pd.application.view.main.MainViewModel
import com.mr.mf_pd.application.view.splash.SplashViewModel
import com.mr.mf_pd.application.view.tev.CheckTEVViewModel
import com.mr.mf_pd.application.view.tev.setting.TEVSettingViewModel
import com.mr.mf_pd.application.view.uhf.CheckUHFViewModel
import com.mr.mf_pd.application.view.uhf.phase.UHFPhaseModelViewModel
import com.mr.mf_pd.application.view.uhf.real.UHFRealModelViewModel
import com.mr.mf_pd.application.view.uhf.setting.UHFSettingViewModel


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
                CheckUHFViewModel(dataRepository)
            isAssignableFrom(UHFRealModelViewModel::class.java) ->
                UHFRealModelViewModel(dataRepository)
            isAssignableFrom(UHFPhaseModelViewModel::class.java) ->
                UHFPhaseModelViewModel(dataRepository)
            isAssignableFrom(UHFSettingViewModel::class.java) ->
                UHFSettingViewModel(dataRepository, settingRepository)
            isAssignableFrom(CheckACViewModel::class.java) ->
                CheckACViewModel(dataRepository)

            isAssignableFrom(ACSettingViewModel::class.java) ->
                ACSettingViewModel(dataRepository, settingRepository)
            isAssignableFrom(ACPhaseModelViewModel::class.java) ->
                ACPhaseModelViewModel(dataRepository)
            isAssignableFrom(TEVContinuityModelViewModel::class.java) ->
                TEVContinuityModelViewModel(dataRepository)
            isAssignableFrom(ACFlightModelViewModel::class.java) ->
                ACFlightModelViewModel(dataRepository)
            isAssignableFrom(ACPulseModelViewModel::class.java) ->
                ACPulseModelViewModel(dataRepository)
            isAssignableFrom(ACRealModelViewModel::class.java) ->
                ACRealModelViewModel(dataRepository)

            isAssignableFrom(HFPhaseModelViewModel::class.java) ->
                TEVPhaseModelViewModel(dataRepository)
            isAssignableFrom(HFRealModelViewModel::class.java) ->
                TEVRealModelViewModel(dataRepository)
            isAssignableFrom(HFSettingViewModel::class.java) ->
                HFSettingViewModel(dataRepository, settingRepository)
            isAssignableFrom(CheckHFViewModel::class.java) ->
                CheckHFViewModel(dataRepository)

            isAssignableFrom(CheckTEVViewModel::class.java) ->
                CheckTEVViewModel(dataRepository)
            isAssignableFrom(TEVSettingViewModel::class.java) ->
                TEVSettingViewModel(dataRepository, settingRepository)
            isAssignableFrom(TEVContinuityModelViewModel::class.java) ->
                TEVContinuityModelViewModel(dataRepository)
            isAssignableFrom(TEVPhaseModelViewModel::class.java) ->
                TEVPhaseModelViewModel(dataRepository)
            isAssignableFrom(TEVRealModelViewModel::class.java) ->
                TEVRealModelViewModel(dataRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
