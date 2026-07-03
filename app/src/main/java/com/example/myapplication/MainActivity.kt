package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF4F6F8) // Light greyish background
                ) { innerPadding ->
                    PantallaRetoAgente(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// -- Colors for Traffic Theme --
val PrimaryPoliceBlue = Color(0xFF153448)
val AccentAmber = Color(0xFFFFB200)
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF2C3E50)
val SuccessGreen = Color(0xFF2E7D32)

@Suppress("DEPRECATION")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PantallaRetoAgente(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- ESTADOS DEL FORMULARIO ---
    var tipoAccidente by remember { mutableStateOf("Choque") }
    var fecha by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())) }
    var matricula by remember { mutableStateOf("") }
    var nombreConductor by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    
    // --- ESTADOS DE CÁMARA Y UBICACIÓN ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var ubicacionGps by remember { mutableStateOf("Toca el botón para ubicar") }
    var isLocationLoading by remember { mutableStateOf(false) }

    // --- GESTIÓN DE PERMISOS ---
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // --- LÓGICA DE CÁMARA ---
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri?.let { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    capturedBitmap = BitmapFactory.decodeStream(it)
                }
            }
        }
    }

    fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File.createTempFile("SINIESTRO_${timeStamp}_", ".jpg", context.getExternalFilesDir(null))
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // --- LÓGICA DE UBICACIÓN ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
            return
        }
        isLocationLoading = true
        val cts = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                isLocationLoading = false
                ubicacionGps = if (loc != null) "Lat: ${String.format(Locale.getDefault(), "%.5f", loc.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.5f", loc.longitude)}" else "GPS desactivado / No disponible"
            }
            .addOnFailureListener {
                isLocationLoading = false
                ubicacionGps = "Error al obtener ubicación"
            }
    }

    // --- LÓGICA DE GUARDAR Y VIBRAR (5 SEGUNDOS) ---
    fun finalizarYVibrar() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Vibrar durante 5000 milisegundos (5 segundos)
        vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
        
        Toast.makeText(context, "Registro guardado correctamente", Toast.LENGTH_LONG).show()
    }

    // --- INTERFAZ DEL FORMULARIO ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalPolice,
                contentDescription = "Traffic Agent",
                tint = PrimaryPoliceBlue,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Registro de Siniestros",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryPoliceBlue,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Formulario oficial de tránsito",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Animated Permissions Warning
        AnimatedVisibility(
            visible = !permissionsState.allPermissionsGranted,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Warning, contentDescription = "Warning", tint = Color(0xFF856404))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Se requieren permisos de Cámara y GPS para un registro completo.",
                        color = Color(0xFF856404),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 1. Selector de Tipo (Card)
        FormCard(title = "Tipo de Siniestro", icon = Icons.Rounded.MinorCrash) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = tipoAccidente,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccione") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPoliceBlue,
                        focusedLabelColor = PrimaryPoliceBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded, 
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceWhite)
                ) {
                    listOf("Choque", "Colisión", "Atropello").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item, fontSize = 16.sp) },
                            onClick = { tipoAccidente = item; expanded = false }
                        )
                    }
                }
            }
        }

        // 2. Datos Generales
        FormCard(title = "Datos Generales", icon = Icons.Rounded.Article) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(value = fecha, onValueChange = { fecha = it }, label = "Fecha y Hora", icon = Icons.Rounded.CalendarToday)
                CustomTextField(value = matricula, onValueChange = { matricula = it }, label = "Matrícula del auto", icon = Icons.Rounded.DirectionsCar)
                CustomTextField(value = nombreConductor, onValueChange = { nombreConductor = it }, label = "Nombre del conductor", icon = Icons.Rounded.Person)
                CustomTextField(value = cedula, onValueChange = { cedula = it }, label = "Cédula del conductor", icon = Icons.Rounded.Badge)
                CustomTextField(value = observaciones, onValueChange = { observaciones = it }, label = "Observaciones", icon = Icons.Rounded.Notes, minLines = 3)
            }
        }

        // 3. Panel de Cámara
        FormCard(title = "Evidencia Fotográfica", icon = Icons.Rounded.AddAPhoto) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = capturedBitmap != null) {
                    capturedBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Evidencia",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 16.dp)
                        )
                    }
                }
                
                Button(
                    onClick = {
                        if (permissionsState.allPermissionsGranted) {
                            val uri = createImageFile()
                            imageUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPoliceBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (capturedBitmap == null) Icons.Rounded.CameraAlt else Icons.Rounded.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (capturedBitmap == null) "Tomar Fotografía" else "Volver a tomar")
                }
            }
        }

        // 4. Panel de GPS
        FormCard(title = "Ubicación del Suceso", icon = Icons.Rounded.PinDrop) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Coordenadas actuales:", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = if (isLocationLoading) "Buscando satélites..." else ubicacionGps,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                FilledIconButton(
                    onClick = { obtenerUbicacion() },
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentAmber, contentColor = Color.White)
                ) {
                    if (isLocationLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.MyLocation, contentDescription = "Obtener GPS")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Botón Guardar y Vibrar
        Button(
            onClick = { finalizarYVibrar() },
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("GUARDAR REPORTE", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun FormCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryPoliceBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            content()
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryPoliceBlue.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPoliceBlue,
            focusedLabelColor = PrimaryPoliceBlue,
            unfocusedBorderColor = Color.LightGray
        )
    )
}
