package com.mr.mf_pd.application.repository

import com.mr.mf_pd.application.repository.impl.UserRepository

class DefaultUserRepository: UserRepository {

    override fun isLogin(): Int {
        return 0
    }
}