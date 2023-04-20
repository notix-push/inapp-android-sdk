package co.notix.interstitial

import android.content.Context
import android.content.Intent
import co.notix.log.Logger

public class InterstitialContract {

    public companion object {
        public const val RESULT_CLICKED: Int = 1234
        public const val RESULT_DISMISSED: Int = 5678
        public const val RESULT_ERROR: Int = 6543
        public const val REQUEST_CODE: Int = 6548

        public fun createIntent(context: Context, input: InterstitialData?): Intent =
            Intent(context, InterstitialActivity::class.java)
                .also { Logger.v("data before put: $input") }
                .apply { putExtra(InterstitialActivity.DATA, input) }

        public fun parseResult(resultCode: Int, intent: Intent?): InterstitialResult =
            when (resultCode) {
                RESULT_CLICKED -> InterstitialResult.Clicked
                RESULT_DISMISSED -> InterstitialResult.Dismissed
                else -> InterstitialResult.Error
            }
    }
}

public enum class InterstitialResult {
    Clicked, Dismissed, Error
}
