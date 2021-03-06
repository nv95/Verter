package org.koitharu.verter.interactor

import kotlinx.coroutines.flow.*
import org.koitharu.verter.core.db.AppDatabase
import org.koitharu.verter.core.db.entity.toDevice
import org.koitharu.verter.core.db.entity.toEntity
import org.koitharu.verter.core.devices.RemoteDevice
import org.koitharu.verter.core.ssh.SshConnection
import org.koitharu.verter.core.ssh.SshConnectionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInteractor @Inject constructor(
	private val database: AppDatabase,
	private val connectionManager: SshConnectionManager,
) {

	private val connectionStateFlow = MutableStateFlow<SshConnection?>(null)

	val currentConnection: SshConnection?
		get() = connectionStateFlow.value

	val currentDevice: RemoteDevice?
		get() = currentConnection?.deviceInfo

	fun getCurrentConnectionAsFlow(): Flow<SshConnection?> {
		return connectionStateFlow.asStateFlow()
	}

	suspend fun getLastConnectedDevice(): RemoteDevice? {
		return database.devicesDao.getLastOrNull()?.toDevice()
	}

	suspend fun markDeviceConnected(device: RemoteDevice) {
		database.devicesDao.setConnectedAt(device.id, System.currentTimeMillis())
	}

	fun getActiveConnectionAsFlow(): Flow<SshConnection?> {
		return connectionStateFlow.flatMapLatest { conn ->
			conn?.getIsConnectedAsFlow()?.map { isConnected ->
				if (isConnected) conn else null
			} ?: flowOf(null)
		}.distinctUntilChanged()
	}

	fun getCurrentDeviceAsFlow(): Flow<RemoteDevice?> {
		return connectionStateFlow.map { it?.deviceInfo }
	}

	fun obtainConnection(device: RemoteDevice): SshConnection {
		currentConnection?.let {
			if (it.deviceInfo != device) {
				connectionManager.closeConnection(it.deviceInfo)
			}
		}
		return connectionManager.getConnection(device).also {
			connectionStateFlow.value = it
		}
	}

	fun closeCurrentConnection() {
		currentConnection?.let {
			connectionManager.closeConnection(it.deviceInfo)
		}
		connectionStateFlow.value = null
	}

	fun requireConnection(): SshConnection {
		return checkNotNull(currentConnection) {
			"Connection is not established"
		}
	}

	suspend fun addDevice(device: RemoteDevice) {
		database.devicesDao.insert(device.toEntity(connectedAt = 0L))
	}

	fun observeDevices(): Flow<List<RemoteDevice>> {
		return database.devicesDao.observeAll().map { list ->
			list.map { x -> x.toDevice() }
		}
	}
}