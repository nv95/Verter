package org.koitharu.verter.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.koitharu.verter.R

enum class Screens(
	val route: String,
	@StringRes val resourceId: Int,
	@DrawableRes val iconId: Int,
) {
	ACTIONS("actions", R.string.actions, R.drawable.ic_actions),
	MEDIA("media", R.string.player, R.drawable.ic_media_player),
	;
}