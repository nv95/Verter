package org.koitharu.verter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.verter.interactor.DeviceInteractor
import org.koitharu.verter.ui.devices.DeviceEditor
import org.koitharu.verter.ui.MainScreen
import org.koitharu.verter.ui.actions.editor.ActionEditor
import org.koitharu.verter.ui.common.NavBridge
import org.koitharu.verter.ui.common.theme.VerterTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	@Inject
	lateinit var interactor: DeviceInteractor

	@Inject
	lateinit var navBridge: NavBridge

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			VerterTheme {
				val navController = rememberNavController()
				navBridge.bind(navController)
				NavHost(navController = navController, startDestination = NavBridge.Target.MAIN.route) {
					composable(NavBridge.Target.MAIN.route) { MainScreen() }
					composable(NavBridge.Target.DEVICE_EDITOR.route) { DeviceEditor(navController, interactor) }
					composable(NavBridge.Target.ACTION_EDITOR.route) { ActionEditor(navController) }
				}
			}
		}
	}
}