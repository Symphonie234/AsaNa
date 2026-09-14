package ph.asana.app.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val CATEGORY = "category/{slug}/{name}"
    const val SERVICE_DETAIL = "service/{slug}"

    fun category(slug: String, name: String) = "category/$slug/${Uri.encode(name)}"

    fun serviceDetail(slug: String) = "service/$slug"
}
