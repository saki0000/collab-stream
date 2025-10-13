package org.example.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Properties for configuring bottom sheet behavior.
 * These properties are converted to DialogProperties for use with DialogNavigator.
 */
data class BottomSheetProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val usePlatformDefaultWidth: Boolean = true,
)

/**
 * Converts BottomSheetProperties to DialogProperties.
 * This allows us to reuse DialogNavigator for bottom sheet navigation.
 */
fun BottomSheetProperties.toDialogProperties() = DialogProperties(
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
)

/**
 * Extension function for type-safe bottom sheet navigation destination.
 *
 * This reuses the DialogNavigator infrastructure but displays a ModalBottomSheet instead.
 * It provides all the benefits of dialog destinations:
 * - Dedicated NavBackStackEntry
 * - Scoped lifecycle
 * - ViewModelStoreOwner
 * - Proper dismissal and back stack management
 *
 * Example usage:
 * ```
 * NavHost(navController, startDestination = HomeRoute) {
 *     composable<HomeRoute> { HomeScreen() }
 *     bottomSheet<SearchRoute> { SearchBottomSheet(onDismiss = navController::popBackStack) }
 * }
 * ```
 */
inline fun <reified T : Any> NavGraphBuilder.bottomSheet(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    bottomSheetProperties: BottomSheetProperties = BottomSheetProperties(),
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = T::class,
        typeMap = typeMap,
        deepLinks = deepLinks,
        dialogProperties = bottomSheetProperties.toDialogProperties(),
        content = content,
    )
}

/**
 * Extension function for bottom sheet navigation destination with KClass route.
 *
 * Similar to the reified version but allows passing KClass explicitly.
 */
fun <T : Any> NavGraphBuilder.bottomSheet(
    route: KClass<T>,
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    bottomSheetProperties: BottomSheetProperties = BottomSheetProperties(),
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = route,
        typeMap = typeMap,
        deepLinks = deepLinks,
        dialogProperties = bottomSheetProperties.toDialogProperties(),
        content = content,
    )
}

/**
 * Extension function for string-based bottom sheet navigation destination.
 *
 * Useful for legacy or simple string-based routing.
 */
fun NavGraphBuilder.bottomSheet(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    bottomSheetProperties: BottomSheetProperties = BottomSheetProperties(),
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        dialogProperties = bottomSheetProperties.toDialogProperties(),
        content = content,
    )
}
