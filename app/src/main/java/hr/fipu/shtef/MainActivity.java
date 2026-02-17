package hr.fipu.shtef;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private boolean keepSplashScreen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // If savedInstanceState is NOT null, the activity is being recreated (e.g., language change)
        // In this case, we skip the 5-second delay.
        if (savedInstanceState != null) {
            keepSplashScreen = false;
        } else {
            // Keep splash screen on for 5 seconds only on fresh start
            splashScreen.setKeepOnScreenCondition(() -> keepSplashScreen);
            new Handler(Looper.getMainLooper()).postDelayed(() -> keepSplashScreen = false, 5000);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_nav);
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);

            // Pop the back stack when clicking a bottom nav item to avoid stacking fragments over details
            navView.setOnItemSelectedListener(item -> {
                navController.popBackStack(R.id.navigation_home, false);
                return NavigationUI.onNavDestinationSelected(item, navController);
            });
        }
    }
}
