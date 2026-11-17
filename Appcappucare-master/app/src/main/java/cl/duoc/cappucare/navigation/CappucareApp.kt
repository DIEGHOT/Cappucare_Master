package cl.duoc.cappucare.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.cappucare.ui.utils.TipoPantalla
import cl.duoc.cappucare.ui.utils.tipoPantalla
import kotlinx.coroutines.launch

/**
 * Marco de la app adaptable al tamaño de pantalla:
 * compacta -> NavigationBar inferior, media -> ModalNavigationDrawer, expandida -> PermanentNavigationDrawer.
 */
@Composable
fun CappucareApp(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val destino = entrada?.destination
    val tipo = windowSizeClass.tipoPantalla()

    val itemActual = itemsNavegacion.firstOrNull { item ->
        destino?.hierarchy?.any { it.route == item.screen.route } == true
    }
    val esPrincipal = itemActual != null
    val titulo = when {
        itemActual != null -> itemActual.titulo
        destino?.route?.startsWith("producto_form") == true ->
            if (entrada?.arguments?.getString(Screen.ProductoForm.ARG_CODIGO) == null) "Nuevo insumo" else "Editar insumo"
        else -> "Cappucare"
    }
    val irA: (Screen) -> Unit = { navegarPrincipal(navController, it) }

    when (tipo) {
        TipoPantalla.COMPACTA -> MarcoPrincipal(
            titulo = titulo,
            esPrincipal = esPrincipal,
            mostrarBottomBar = esPrincipal,
            itemActual = itemActual,
            onMenu = null,
            onIrA = irA,
            navController = navController,
            windowSizeClass = windowSizeClass
        )

        TipoPantalla.MEDIA -> {
            val estadoDrawer = rememberDrawerState(DrawerValue.Closed)
            val alcance = rememberCoroutineScope()
            ModalNavigationDrawer(
                drawerState = estadoDrawer,
                gesturesEnabled = esPrincipal,
                drawerContent = {
                    ModalDrawerSheet {
                        ContenidoDrawer(itemActual) { screen ->
                            irA(screen)
                            alcance.launch { estadoDrawer.close() }
                        }
                    }
                }
            ) {
                MarcoPrincipal(
                    titulo = titulo,
                    esPrincipal = esPrincipal,
                    mostrarBottomBar = false,
                    itemActual = itemActual,
                    onMenu = { alcance.launch { estadoDrawer.open() } },
                    onIrA = irA,
                    navController = navController,
                    windowSizeClass = windowSizeClass
                )
            }
        }

        TipoPantalla.EXPANDIDA -> PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(Modifier.width(260.dp)) {
                    ContenidoDrawer(itemActual, irA)
                }
            }
        ) {
            MarcoPrincipal(
                titulo = titulo,
                esPrincipal = esPrincipal,
                mostrarBottomBar = false,
                itemActual = itemActual,
                onMenu = null,
                onIrA = irA,
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarcoPrincipal(
    titulo: String,
    esPrincipal: Boolean,
    mostrarBottomBar: Boolean,
    itemActual: ItemNavegacion?,
    onMenu: (() -> Unit)?,
    onIrA: (Screen) -> Unit,
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (!esPrincipal) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    } else if (onMenu != null) {
                        IconButton(onClick = onMenu) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = mostrarBottomBar,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                NavigationBar {
                    itemsNavegacion.forEach { item ->
                        NavigationBarItem(
                            selected = item == itemActual,
                            onClick = { onIrA(item.screen) },
                            icon = { Icon(item.icono, contentDescription = item.titulo) },
                            label = { Text(item.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            windowSizeClass = windowSizeClass,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun ContenidoDrawer(itemActual: ItemNavegacion?, onSeleccion: (Screen) -> Unit) {
    Column(Modifier.padding(12.dp)) {
        Text(
            "Cappucare",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        itemsNavegacion.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.titulo) },
                icon = { Icon(item.icono, contentDescription = null) },
                selected = item == itemActual,
                onClick = { onSeleccion(item.screen) }
            )
        }
    }
}

private fun navegarPrincipal(navController: NavHostController, screen: Screen) {
    navController.navigate(screen.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
