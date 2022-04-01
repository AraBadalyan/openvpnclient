package com.hundredstartups.openvpn.cache

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson

object PrefsCacheManager {

    const val PREF_FILE_NAME = "preferences_file_name_consumer"

    private lateinit var sharedPreferences: SharedPreferences
//    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun initialize(applicationContext: Context) {
        var masterKey = MasterKey.Builder(applicationContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            applicationContext,
            PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun putInCache(key: String?, value: String?) {
        sharedPreferences.edit().apply {
            putString(key, value)
        }.apply()
    }

    fun getFromCache(key: String?, defValue: String?): String? {
        return sharedPreferences.getString(key, defValue)
    }

    fun clearCache() {
        sharedPreferences.edit().clear().apply()
    }

    fun clearKeyValue(key: String){
        sharedPreferences.edit().remove(key).apply()
    }

    fun saveObjectToSharedPreference(
        context: Context,
        preferenceFileName: String?,
        serializedObjectKey: String?,
        savedObject: Any?
    ) {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val serializedObject = gson.toJson(savedObject)
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject)
        sharedPreferencesEditor.apply()
    }

    fun <GenericClass> getSavedObjectFromPreference(
        context: Context,
        preferenceFileName: String?,
        preferenceKey: String?,
        classType: Class<GenericClass>?
    ): GenericClass? {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        if (sharedPreferences.contains(preferenceKey)) {
            val gson = Gson()
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType)
        }
        return null
    }






}