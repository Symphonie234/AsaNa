package ph.asana.app.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the Sanctum bearer token in EncryptedSharedPreferences rather than
 * plain SharedPreferences — it's equivalent to a password, not a UI
 * preference, and deserves the same handling.
 */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    fun clear() = prefs.edit().remove(KEY_TOKEN).apply()

    private companion object {
        const val KEY_TOKEN = "token"
    }
}
