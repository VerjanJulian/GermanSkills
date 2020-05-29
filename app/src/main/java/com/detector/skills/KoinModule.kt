package com.detector.skills

import com.detector.skills.viewmodel.SkillsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModules = module {
    // Specific viewModel pattern to tell Koin how to build MainViewModel
    viewModel { SkillsViewModel() }
}