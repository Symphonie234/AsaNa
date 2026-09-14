package ph.asana.app.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val CATEGORY = "category/{slug}/{name}"
    const val SERVICE_DETAIL = "service/{slug}"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val AUTH = "auth"

    /** Top-level destinations shown in the bottom navigation bar. */
    val BOTTOM_NAV_ROUTES = setOf(HOME, FAVORITES, SETTINGS)

    fun category(slug: String, name: String) = "category/$slug/${Uri.encode(name)}"

    fun serviceDetail(slug: String) = "service/$slug"
}
