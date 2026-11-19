package hu.wasteuber.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import hu.wasteuber.app.navigation.AppNavigation

private val WasteUberGreen =
    Color(0xFF00A624)

private val WasteUberGreenDark =
    Color(0xFF087F23)

private val WasteUberGreenLight =
    Color(0xFF43D16B)

private val WasteUberBlack =
    Color(0xFF171717)

private val WasteUberDarkGray =
    Color(0xFF202020)

private val WasteUberLightColors =
    lightColorScheme(
        primary = WasteUberGreen,
        onPrimary = Color.White,

        primaryContainer =
            Color(0xFFD7FFD9),

        onPrimaryContainer =
            Color(0xFF002106),

        secondary =
            WasteUberGreenDark,

        onSecondary =
            Color.White,

        secondaryContainer =
            Color(0xFFC5F8C9),

        onSecondaryContainer =
            Color(0xFF002106),

        tertiary =
            Color(0xFF2E7D32),

        background =
            Color(0xFFF9FBF8),

        onBackground =
            WasteUberBlack,

        surface =
            Color.White,

        onSurface =
            WasteUberBlack,

        surfaceVariant =
            Color(0xFFE8EFE7),

        onSurfaceVariant =
            Color(0xFF414941),

        outline =
            Color(0xFF717971),

        error =
            Color(0xFFBA1A1A),

        onError =
            Color.White
    )

private val WasteUberDarkColors =
    darkColorScheme(
        primary =
            WasteUberGreenLight,

        onPrimary =
            Color(0xFF00390D),

        primaryContainer =
            WasteUberGreenDark,

        onPrimaryContainer =
            Color(0xFFB6F5BE),

        secondary =
            Color(0xFF8EDB9B),

        onSecondary =
            Color(0xFF003913),

        secondaryContainer =
            Color(0xFF165227),

        onSecondaryContainer =
            Color(0xFFA9F5B4),

        tertiary =
            WasteUberGreenLight,

        background =
            Color(0xFF111411),

        onBackground =
            Color(0xFFE1E4DF),

        surface =
            WasteUberDarkGray,

        onSurface =
            Color(0xFFE1E4DF),

        surfaceVariant =
            Color(0xFF414941),

        onSurfaceVariant =
            Color(0xFFC1C9BF),

        outline =
            Color(0xFF8B938A),

        error =
            Color(0xFFFFB4AB),

        onError =
            Color(0xFF690005)
    )

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        /*
         * Android 15+ alatt az alkalmazás
         * edge-to-edge módban működik.
         */
        enableEdgeToEdge()

        setContent {

            WasteUberTheme {

                /*
                 * Az egész alkalmazást a biztonságosan
                 * használható képernyőterületen belül tartjuk.
                 *
                 * Figyelembe veszi többek között:
                 * - státuszsáv
                 * - kameralyuk / notch
                 * - navigációs sáv
                 * - különböző telefonméretek
                 */
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {

                    AppNavigation()
                }
            }
        }
    }
}

@Composable
private fun WasteUberTheme(
    content: @Composable () -> Unit
) {

    val colors =
        if (
            isSystemInDarkTheme()
        ) {
            WasteUberDarkColors
        } else {
            WasteUberLightColors
        }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}