package app.coreply.coreplyapp.applistener

/**
 * Created on 1/18/17.
 */
data class SupportedAppProperty(
    val pkgName: String?,val triggerWidget: String?, val excludeWidgets: Array<String>,val messageWidgets: Array<String>
)
