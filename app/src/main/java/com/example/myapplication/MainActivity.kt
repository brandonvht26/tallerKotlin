package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ComponentesBasicos(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ComponentesBasicos(modifier: Modifier = Modifier) {
    // Estado para los componentes interactivos
    var texto by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var switchActivo by remember { mutableStateOf(false) }
    var contador by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TEXT - Texto simple y estilizado
        Text(
            text = "Componentes Básicos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. TEXTFIELD - Campo de entrada
        TextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Escribe tu nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (texto.isNotEmpty()) {
            Text(text = "Hola, $texto!", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BUTTONS
        Button(onClick = { contador++ }) {
            Text("Presionado: $contador veces")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { contador = 0 }) {
            Text("Reiniciar contador")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CHECKBOX Y SWITCH
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            )
            Text("Acepto los términos")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo Oscuro")
            Switch(
                checked = switchActivo,
                onCheckedChange = { switchActivo = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Esta es una Card", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Las cards son útiles para agrupar contenido relacionado.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para abrir la cámara
        Button(
            onClick = {
                context.startActivity(Intent(context, CameraActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Abrir Cámara")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // NUEVO: Botón para abrir la ubicación
        Button(
            onClick = {
                context.startActivity(Intent(context, LocationActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Abrir Ubicación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. BOX
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium
            ) {}
            Text("Centrado", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Texto dinámico
        Text(
            text = if (switchActivo) "Switch está ON" else "Switch está OFF",
            color = if (switchActivo) Color(0xFF2E7D32) else Color.Red,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentesBasicosPreview() {
    MyApplicationTheme {
        ComponentesBasicos()
    }
}
