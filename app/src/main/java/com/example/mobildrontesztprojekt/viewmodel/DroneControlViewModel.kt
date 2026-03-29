package com.example.mobildrontesztprojekt.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobildrontesztprojekt.data.database.AppDatabase
import com.example.mobildrontesztprojekt.data.entity.AppKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.common.callback.CommonCallbacks
import dji.v5.common.error.IDJIError
import dji.v5.manager.SDKManager
import dji.v5.manager.aircraft.virtualstick.VirtualStickManager
import dji.v5.manager.interfaces.IVirtualStickManager
import dji.v5.manager.KeyManager
import dji.v5.manager.datacenter.camera.CameraStreamManager
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.common.register.DJISDKInitEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── Állapot osztályok ────────────────────────────────────────────────────────

enum class SdkRegisterState {
    IDLE,          // még nem próbálkozott
    REGISTERING,   // folyamatban
    SUCCESS,       // kulcs érvényes, SDK kész
    FAILED         // érvénytelen kulcs vagy hálózati hiba
}

enum class DroneConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class FlightData(
    val altitudeM: Float = 0f,
    val speedMs: Float = 0f,
    val batteryPercent: Int = 0,
    val isFlying: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class DroneControlViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DroneControlVM"
    }

    private val db = AppDatabase.getInstance(application)

    // ── App Key állapot ──────────────────────────────────────────────────────
    val savedAppKey = db.appKeyDao().observe()   // Flow<AppKey?>

    private val _sdkState = MutableStateFlow(SdkRegisterState.IDLE)
    val sdkState: StateFlow<SdkRegisterState> = _sdkState.asStateFlow()

    private val _sdkError = MutableStateFlow<String?>(null)
    val sdkError: StateFlow<String?> = _sdkError.asStateFlow()

    // ── Drón kapcsolat ───────────────────────────────────────────────────────
    private val _connectionState = MutableStateFlow(DroneConnectionState.DISCONNECTED)
    val connectionState: StateFlow<DroneConnectionState> = _connectionState.asStateFlow()

    private val _connectedDroneName = MutableStateFlow<String?>(null)
    val connectedDroneName: StateFlow<String?> = _connectedDroneName.asStateFlow()

    // ── Repülési adatok ──────────────────────────────────────────────────────
    private val _flightData = MutableStateFlow(FlightData())
    val flightData: StateFlow<FlightData> = _flightData.asStateFlow()

    // ── Virtual stick parancsok (értékek: -1.0 .. +1.0) ─────────────────────
    // roll=oldalsó, pitch=előre/hátra, yaw=forgás, throttle=magasság
    private val _roll     = MutableStateFlow(0f)
    private val _pitch    = MutableStateFlow(0f)
    private val _yaw      = MutableStateFlow(0f)
    private val _throttle = MutableStateFlow(0f)
    val roll:     StateFlow<Float> = _roll.asStateFlow()
    val pitch:    StateFlow<Float> = _pitch.asStateFlow()
    val yaw:      StateFlow<Float> = _yaw.asStateFlow()
    val throttle: StateFlow<Float> = _throttle.asStateFlow()

    // ── Virtual stick aktív ──────────────────────────────────────────────────
    private val _virtualStickEnabled = MutableStateFlow(false)
    val virtualStickEnabled: StateFlow<Boolean> = _virtualStickEnabled.asStateFlow()

    // ── Kamera stream ────────────────────────────────────────────────────────
    // A tényleges SurfaceTexture-t a Composable kezeli; itt csak az elérhetőség jelenik meg.
    private val _cameraAvailable = MutableStateFlow(false)
    val cameraAvailable: StateFlow<Boolean> = _cameraAvailable.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    // App Key mentése & SDK regisztrálás
    // ─────────────────────────────────────────────────────────────────────────

    fun saveAndRegisterKey(key: String) {
        if (key.isBlank()) return
        viewModelScope.launch {
            db.appKeyDao().save(AppKey(djiAppKey = key.trim()))
            registerSdk(key.trim())
        }
    }

    fun clearAppKey() {
        viewModelScope.launch {
            db.appKeyDao().clear()
            _sdkState.value = SdkRegisterState.IDLE
            _sdkError.value = null
        }
    }

    private fun registerSdk(appKey: String) {
        _sdkState.value = SdkRegisterState.REGISTERING
        _sdkError.value = null

        // FIX: SDKManager.getInstance().init() in MSDK V5 takes only (Context, SDKManagerCallback).
        // The App Key is declared in AndroidManifest.xml as a <meta-data> tag, not passed here.
        SDKManager.getInstance().init(
            getApplication(),
            object : SDKManagerCallback {
                override fun onRegisterSuccess() {
                    Log.d(TAG, "SDK regisztráció sikeres")
                    _sdkState.value = SdkRegisterState.SUCCESS
                    listenConnectionState()
                }

                override fun onRegisterFailure(error: IDJIError?) {
                    Log.e(TAG, "SDK regisztráció hiba: $error")
                    _sdkState.value = SdkRegisterState.FAILED
                    _sdkError.value = error?.description() ?: "Ismeretlen hiba"
                }

                override fun onProductDisconnect(productId: Int) {
                    Log.d(TAG, "Termék lecsatlakozva: $productId")
                    _connectionState.value = DroneConnectionState.DISCONNECTED
                    _connectedDroneName.value = null
                    _cameraAvailable.value = false
                    _virtualStickEnabled.value = false
                }

                override fun onProductConnect(productId: Int) {
                    Log.d(TAG, "Termék csatlakozva: $productId")
                    _connectionState.value = DroneConnectionState.CONNECTED
                    _cameraAvailable.value = true
                    listenFlightData()
                }

                override fun onProductChanged(productId: Int) {}

                // FIX: InitEvent lives in dji.v5.manager.SDKManager.InitEvent – use the
                // fully-qualified nested type so the compiler can resolve it.
                override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {}

                override fun onDatabaseDownloadProgress(current: Long, total: Long) {}
            }
        )

        // FIX: The App Key cannot be injected at runtime via init().
        // You must add it to AndroidManifest.xml inside <application>:
        //
        //   <meta-data
        //       android:name="com.dji.sdk.API_KEY"
        //       android:value="YOUR_APP_KEY_HERE" />
        //
        // If you need to support multiple / user-supplied keys you would need to
        // restart the process after writing the new key to SharedPreferences and
        // reading it in a custom Application.onCreate() before calling init().
        //
        // For now, saveAndRegisterKey() saves the key to the Room database so the
        // user can see which key is active, and calls init() to start the SDK.
        // The key that is actually used by MSDK is the one in the manifest.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kapcsolat figyelése – DJI SDK V5 KeyManager alapon
    // ─────────────────────────────────────────────────────────────────────────

    private fun listenConnectionState() {
        // Opcionális: aircraftName lekérdezés a Key API-n át
        val nameKey = KeyTools.createKey(FlightControllerKey.KeyAircraftName)
        KeyManager.getInstance().getValue(
            nameKey,
            object : CommonCallbacks.CompletionCallbackWithParam<String> {
                override fun onSuccess(name: String?) {
                    _connectedDroneName.value = name
                }
                override fun onFailure(error: IDJIError) {
                    Log.w(TAG, "Drón névlekérdezés sikertelen: $error")
                }
            }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Repülési telemetria figyelése
    // ─────────────────────────────────────────────────────────────────────────

    private fun listenFlightData() {
        // Magasság
        val altKey = KeyTools.createKey(FlightControllerKey.KeyAltitude)
        KeyManager.getInstance().listen(altKey, this) { _, newValue ->
            newValue?.let { _flightData.value = _flightData.value.copy(altitudeM = it.toFloat()) }
        }
        // Sebesség (horizontális)
        val speedKey = KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity)
        KeyManager.getInstance().listen(speedKey, this) { _, newValue ->
            newValue?.let { v ->
                val speed = Math.sqrt((v.x * v.x + v.y * v.y).toDouble()).toFloat()
                _flightData.value = _flightData.value.copy(speedMs = speed)
            }
        }
        // Repül-e?
        val flyingKey = KeyTools.createKey(FlightControllerKey.KeyIsFlying)
        KeyManager.getInstance().listen(flyingKey, this) { _, newValue ->
            newValue?.let { _flightData.value = _flightData.value.copy(isFlying = it) }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Virtual Stick – manuális vezérlés
    // ─────────────────────────────────────────────────────────────────────────

    fun enableVirtualStick() {
        if (_connectionState.value != DroneConnectionState.CONNECTED) return
        VirtualStickManager.getInstance().enableVirtualStick(
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    _virtualStickEnabled.value = true
                    Log.d(TAG, "VirtualStick engedélyezve")
                }
                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "VirtualStick engedélyezési hiba: $error")
                }
            }
        )
    }

    fun disableVirtualStick() {
        VirtualStickManager.getInstance().disableVirtualStick(
            object : CommonCallbacks.CompletionCallback {
                override fun onSuccess() {
                    _virtualStickEnabled.value = false
                    sendStickValues(0f, 0f, 0f, 0f)
                    Log.d(TAG, "VirtualStick kikapcsolva")
                }
                override fun onFailure(error: IDJIError) {
                    Log.e(TAG, "VirtualStick kikapcsolási hiba: $error")
                }
            }
        )
    }

    /**
     * Joystick értékek frissítése.
     * Az értékeket a UI küldi, -1.0..+1.0 tartományban.
     * Az SDK a tényleges paranccsá skálázza (max 15 m/s, 100°/s stb.).
     */
    fun updateLeftStick(vertical: Float, horizontal: Float) {
        _throttle.value = vertical.coerceIn(-1f, 1f)
        _yaw.value      = horizontal.coerceIn(-1f, 1f)
        if (_virtualStickEnabled.value) sendCurrentStickValues()
    }

    fun updateRightStick(vertical: Float, horizontal: Float) {
        _pitch.value = vertical.coerceIn(-1f, 1f)
        _roll.value  = horizontal.coerceIn(-1f, 1f)
        if (_virtualStickEnabled.value) sendCurrentStickValues()
    }

    private fun sendCurrentStickValues() {
        sendStickValues(_roll.value, _pitch.value, _yaw.value, _throttle.value)
    }

    private fun sendStickValues(roll: Float, pitch: Float, yaw: Float, throttle: Float) {
        val manager: IVirtualStickManager = VirtualStickManager.getInstance()
        manager.leftStick.verticalPosition   = (throttle * 4.0).toInt()   // ±4 m/s
        manager.leftStick.horizontalPosition = (yaw      * 30.0).toInt()  // ±30 °/s
        manager.rightStick.verticalPosition  = (pitch    * 15.0).toInt()  // ±15 m/s
        manager.rightStick.horizontalPosition= (roll     * 15.0).toInt()  // ±15 m/s
    }

    // Stick nullázása (ujj felemelt)
    fun centerSticks() {
        _roll.value = 0f; _pitch.value = 0f
        _yaw.value  = 0f; _throttle.value = 0f
        if (_virtualStickEnabled.value) sendStickValues(0f, 0f, 0f, 0f)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kamera stream – CameraStreamManager
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A Composable meghívja, amikor a Surface kész.
     * A CameraStreamManager a textureView-t kapja a DJI SDK-ban;
     * Compose-ban AndroidView(::TextureView) wrappelve adjuk át.
     */
    fun startCameraStream(textureView: android.view.TextureView) {
        if (!_cameraAvailable.value) return
        val surface = android.view.Surface(textureView.surfaceTexture ?: return)
        CameraStreamManager.getInstance().putCameraStreamSurface(
            ComponentIndexType.LEFT_OR_MAIN,
            surface,
            textureView.width,
            textureView.height,
            ICameraStreamManager.ScaleType.CENTER_INSIDE
        )
    }

    fun stopCameraStream(textureView: android.view.TextureView) {
        val surface = android.view.Surface(textureView.surfaceTexture ?: return)
        CameraStreamManager.getInstance().removeCameraStreamSurface(surface)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cleanup
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Listener-ek eltávolítása
        KeyManager.getInstance().cancelListen(this)
        if (_virtualStickEnabled.value) {
            disableVirtualStick()
        }
    }
}