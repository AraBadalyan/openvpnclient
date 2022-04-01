package com.hundredstartups.openvpn.cache

object UserAccountManager {

    private const val GOOGLE_TOKEN = "google_token"
    private const val GOOGLE_USER_ID = "google_user_id"
    private const val GOOGLE_EMAIL = "google_email"

    /**
     * AccessToken - Expire time is 120 min
     */
    fun saveUserData(token: String?, userId: String?, email: String?) {
        PrefsCacheManager.putInCache(GOOGLE_TOKEN, token)
        PrefsCacheManager.putInCache(GOOGLE_USER_ID, userId)
        PrefsCacheManager.putInCache(GOOGLE_EMAIL, email)
    }

    fun getToken(): String? {
        return PrefsCacheManager.getFromCache(GOOGLE_TOKEN, "")
    }

    fun getUserId(): String? {
        return PrefsCacheManager.getFromCache(GOOGLE_USER_ID, "")
    }

    fun getEmail(): String? {
        return PrefsCacheManager.getFromCache(GOOGLE_EMAIL, "")
    }

    fun clearCache() {
        saveUserData(null, null, null)
        PrefsCacheManager.clearCache()
    }
}