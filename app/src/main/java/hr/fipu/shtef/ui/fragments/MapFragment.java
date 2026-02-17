package hr.fipu.shtef.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

import hr.fipu.shtef.R;
import hr.fipu.shtef.data.remote.ApiService;
import hr.fipu.shtef.domain.model.ServiceCenter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment {

    private MapView mapView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String targetSupplier;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        if (getArguments() != null) {
            targetSupplier = getArguments().getString("supplierName");
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/jseketa-fipu/shtef/main/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = root.findViewById(R.id.map_view);
        progressBar = root.findViewById(R.id.map_progress);
        
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        
        if (targetSupplier != null) {
            // Hide map initially to avoid showing default location
            mapView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            // Default center
            mapController.setZoom(10.0);
            GeoPoint startPoint = new GeoPoint(44.8666, 13.8496);
            mapController.setCenter(startPoint);
        }

        fetchServiceCenters();

        return root;
    }

    private void fetchServiceCenters() {
        apiService.getServiceCenters().enqueue(new Callback<List<ServiceCenter>>() {
            @Override
            public void onResponse(Call<List<ServiceCenter>> call, Response<List<ServiceCenter>> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    displayMarkers(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ServiceCenter>> call, Throwable t) {
                Log.e("MapFragment", "Error fetching service centers", t);
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void displayMarkers(List<ServiceCenter> centers) {
        if (mapView == null) return;
        
        mapView.getOverlays().clear();
        Marker targetMarker = null;

        for (ServiceCenter center : centers) {
            GeoPoint point = new GeoPoint(center.getLatitude(), center.getLongitude());
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(center.getName());
            marker.setSnippet(center.getAddress());
            mapView.getOverlays().add(marker);

            if (targetSupplier != null) {
                String s1 = center.getName().toLowerCase();
                String s2 = targetSupplier.toLowerCase();
                if (s1.contains(s2) || s2.contains(s1)) {
                    targetMarker = marker;
                }
            }
        }

        if (targetMarker != null) {
            // Set position and zoom IMMEDIATELY before showing the map
            mapView.getController().setZoom(18.0);
            mapView.getController().setCenter(targetMarker.getPosition());
            targetMarker.showInfoWindow();
            
            mapView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else if (targetSupplier != null) {
            // If we had a target but didn't find it, just show the map at default
            mapView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); 
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDetach();
    }
}
