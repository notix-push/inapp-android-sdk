package co.notix.domain

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
public data class RequestVars(
    var var1: String? = null,
    var var2: String? = null,
    var var3: String? = null,
    var var4: String? = null,
    var var5: String? = null,
)

internal fun JSONObject.putRequestVars(vars: RequestVars) {
    put("var_1", vars.var1)
    put("var_2", vars.var2)
    put("var_3", vars.var3)
    put("var_4", vars.var4)
    put("var_5", vars.var5)
}