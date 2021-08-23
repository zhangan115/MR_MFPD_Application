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
package com.mr.mf_pd.application.utils

/**
 * Extension functions for Fragment.
 */

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.factory.ViewModelFactory


fun Fragment.getViewModelFactory(): ViewModelFactory {
    val dataRepository = (requireContext().applicationContext as MRApplication).dataRepository
    val filesRepository = (requireContext().applicationContext as MRApplication).filesRepository
    val settingRepository = (requireContext().applicationContext as MRApplication).settingRepository
    val userRepository = (requireContext().applicationContext as MRApplication).userRepository
    return ViewModelFactory(
        dataRepository,
        filesRepository,
        settingRepository,
        userRepository,
        this
    )
}

fun AppCompatActivity.getViewModelFactory(): ViewModelFactory {
    val dataRepository = (applicationContext as MRApplication).dataRepository
    val filesRepository = (applicationContext as MRApplication).filesRepository
    val settingRepository = (applicationContext as MRApplication).settingRepository
    val userRepository = (applicationContext as MRApplication).userRepository
    return ViewModelFactory(
        dataRepository,
        filesRepository,
        settingRepository,
        userRepository,
        this
    )
}
