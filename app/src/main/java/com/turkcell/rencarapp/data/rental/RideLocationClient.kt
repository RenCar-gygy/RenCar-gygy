package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.session.SessionStore
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Aktif yolculuktaki aracın canlı konumunu dinleyen Socket.IO istemcisi.
 *
 * Sunucu sözleşmesi: `/ws/locations` namespace'ine CUSTOMER token'ıyla bağlanılır;
 * yalnızca kendi aktif kiralamasındaki aracın konumu `my-vehicle` event'iyle gelir.
 * Aktif kiralama yoksa event gelmez — akış sessiz kalır.
 */
@Singleton
class RideLocationClient @Inject constructor(
    private val sessionStore: SessionStore,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
    @Named("socketLocationsUrl") private val socketLocationsUrl: String,
) {
    fun vehiclePositionStream(): Flow<VehiclePoint> = callbackFlow {
        var socket: Socket? = null
        var triedRefresh = false

        fun teardown() {
            socket?.let {
                it.off()
                it.disconnect()
                it.close()
            }
            socket = null
        }

        fun connectWith(token: String) {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                forceNew = true
                reconnection = true
            }
            val newSocket = IO.socket(socketLocationsUrl, options)

            newSocket.on(MY_VEHICLE_EVENT) { args ->
                parsePoint(args)?.let { point -> trySend(point) }
            }
            newSocket.on(Socket.EVENT_CONNECT_ERROR) {
                if (!triedRefresh) {
                    triedRefresh = true
                    launch {
                        val refreshed = authorizedRequestExecutor.refreshCurrentSession()
                        teardown()
                        val freshToken = refreshed?.accessToken ?: sessionStore.getSession()?.accessToken
                        if (freshToken != null) {
                            connectWith(freshToken)
                        } else {
                            close()
                        }
                    }
                }
            }
            socket = newSocket
            newSocket.connect()
        }

        val token = sessionStore.getSession()?.accessToken
        if (token == null) {
            close()
        } else {
            connectWith(token)
        }

        awaitClose { teardown() }
    }

    private fun parsePoint(args: Array<Any?>): VehiclePoint? {
        val root = args.getOrNull(0) as? JSONObject ?: return null
        val vehicle = root.optJSONObject("vehicle") ?: return null
        val latitude = vehicle.optDouble("latitude", Double.NaN)
        val longitude = vehicle.optDouble("longitude", Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) return null
        return VehiclePoint(latitude = latitude, longitude = longitude)
    }

    private companion object {
        const val MY_VEHICLE_EVENT = "my-vehicle"
    }
}
