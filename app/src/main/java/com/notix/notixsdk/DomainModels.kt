package com.notix.notixsdk

import org.json.JSONObject

class DomainModels {
    data class RequestVars(
        var var1: String = "",
        var var2: String = "",
        var var3: String = "",
        var var4: String = "",
        var var5: String = "",
    ) {
        fun fillJsonObject(jobj: JSONObject) {
            if (var1.isNotEmpty()) {
                jobj.put("var_1", var1)
            }
            if (var2.isNotEmpty()) {
                jobj.put("var_2", var2)
            }
            if (var3.isNotEmpty()) {
                jobj.put("var_3", var3)
            }
            if (var4.isNotEmpty()) {
                jobj.put("var_4", var4)
            }
            if (var5.isNotEmpty()) {
                jobj.put("var_5", var5)
            }
        }

        fun isEmpty(): Boolean {
            return var1.isEmpty() && var2.isEmpty() && var3.isEmpty() && var4.isEmpty() && var5.isEmpty()
        }
    }
}