package hr.fipu.shtef.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import hr.fipu.shtef.data.local.entity.MachineEntity;
import hr.fipu.shtef.data.local.entity.PartEntity;
import hr.fipu.shtef.data.repository.MachineRepository;
import hr.fipu.shtef.domain.model.ServiceCenter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MachineViewModel extends AndroidViewModel {
    private final MachineRepository repository;

    public MachineViewModel(@NonNull Application application) {
        super(application);
        repository = new MachineRepository(application);
    }

    public LiveData<List<MachineEntity>> getAllMachines() {
        return repository.getAllMachines();
    }

    public LiveData<MachineEntity> getMachineByEan(String ean) {
        return repository.getMachineByEan(ean);
    }

    public LiveData<MachineEntity> getMachineById(String id) {
        return repository.getMachineById(id);
    }

    public LiveData<List<MachineEntity>> searchMachines(String query) {
        return repository.searchMachines(query);
    }

    public LiveData<List<MachineEntity>> getFavoriteMachines() {
        return repository.getFavoriteMachines();
    }

    public void toggleFavorite(MachineEntity machine) {
        repository.updateFavoriteStatus(machine.id, !machine.isFavorite);
    }

    public LiveData<List<PartEntity>> getPartsForMachine(String machineId) {
        return repository.getPartsForMachine(machineId);
    }

    public void getServiceCenters(Callback<List<ServiceCenter>> callback) {
        repository.getServiceCenters(callback);
    }
}
