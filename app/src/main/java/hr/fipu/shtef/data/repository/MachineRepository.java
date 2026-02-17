package hr.fipu.shtef.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hr.fipu.shtef.data.local.AppDatabase;
import hr.fipu.shtef.data.local.dao.MachineDao;
import hr.fipu.shtef.data.local.entity.MachineEntity;
import hr.fipu.shtef.data.local.entity.PartEntity;
import hr.fipu.shtef.data.remote.ApiService;
import hr.fipu.shtef.domain.model.Machine;
import hr.fipu.shtef.domain.model.ServiceCenter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MachineRepository {
    private static final String TAG = "MachineRepository";
    private final MachineDao machineDao;
    private final ApiService apiService;
    private final ExecutorService executorService;

    public MachineRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        machineDao = db.machineDao();
        executorService = Executors.newFixedThreadPool(4);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/jseketa-fipu/shtef/main/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Fetch all data from GitHub on startup
        refreshDataFromGitHub();
    }

    private void refreshDataFromGitHub() {
        apiService.getAllMachines().enqueue(new Callback<List<Machine>>() {
            @Override
            public void onResponse(Call<List<Machine>> call, Response<List<Machine>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> {
                        for (Machine m : response.body()) {
                            saveMachinePreservingFavorite(m);
                            if (m.getParts() != null) {
                                for (PartEntity part : m.getParts()) {
                                    part.machineId = m.getId(); // Ensure link is correct
                                }
                                machineDao.insertParts(m.getParts());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Machine>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch data from GitHub", t);
            }
        });
    }

    public LiveData<List<MachineEntity>> getAllMachines() {
        return machineDao.getAllMachines();
    }

    public LiveData<MachineEntity> getMachineByEan(String ean) {
        return machineDao.getMachineByEan(ean);
    }

    public MachineEntity getMachineByEanImmediate(String ean) {
        return machineDao.getMachineByEanImmediate(ean);
    }

    public LiveData<MachineEntity> getMachineById(String id) {
        return machineDao.getMachineById(id);
    }

    public MachineEntity getMachineByIdImmediate(String id) {
        return machineDao.getMachineByIdImmediate(id);
    }

    public LiveData<List<MachineEntity>> searchMachines(String query) {
        return machineDao.searchMachines("%" + query + "%");
    }

    public LiveData<List<MachineEntity>> getFavoriteMachines() {
        return machineDao.getFavoriteMachines();
    }

    public void updateFavoriteStatus(String id, boolean isFavorite) {
        executorService.execute(() -> machineDao.updateFavoriteStatus(id, isFavorite));
    }

    private void saveMachinePreservingFavorite(Machine m) {
        MachineEntity existing = machineDao.getMachineByIdImmediate(m.getId());
        MachineEntity entity = new MachineEntity();
        entity.id = m.getId();
        entity.brand = m.getBrand();
        entity.model = m.getModel();
        entity.ean = m.getEan();
        entity.repairabilityScore = m.getRepairabilityScore();
        entity.description = m.getDescription();
        entity.imageUrl = m.getImageUrl();
        entity.lastUpdated = System.currentTimeMillis();

        if (existing != null) {
            entity.isFavorite = existing.isFavorite;
        }
        machineDao.insertMachine(entity);
    }

    public LiveData<List<PartEntity>> getPartsForMachine(String machineId) {
        return machineDao.getPartsForMachine(machineId);
    }

    public void getServiceCenters(Callback<List<ServiceCenter>> callback) {
        apiService.getServiceCenters().enqueue(callback);
    }
}
