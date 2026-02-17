package hr.fipu.shtef.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import hr.fipu.shtef.data.local.entity.MachineEntity;
import hr.fipu.shtef.data.local.entity.PartEntity;

@Dao
public interface MachineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMachine(MachineEntity machine);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertParts(List<PartEntity> parts);

    @Query("SELECT * FROM machines WHERE id = :id")
    LiveData<MachineEntity> getMachineById(String id);

    @Query("SELECT * FROM machines WHERE id = :id")
    MachineEntity getMachineByIdImmediate(String id);

    @Query("SELECT * FROM machines WHERE ean = :ean")
    LiveData<MachineEntity> getMachineByEan(String ean);

    @Query("SELECT * FROM machines WHERE ean = :ean")
    MachineEntity getMachineByEanImmediate(String ean);

    @Query("SELECT * FROM machines WHERE brand LIKE :query OR model LIKE :query")
    LiveData<List<MachineEntity>> searchMachines(String query);

    @Query("SELECT * FROM parts WHERE machineId = :machineId")
    LiveData<List<PartEntity>> getPartsForMachine(String machineId);

    @Query("SELECT * FROM machines ORDER BY lastUpdated DESC")
    LiveData<List<MachineEntity>> getAllMachines();

    @Query("SELECT * FROM machines WHERE isFavorite = 1")
    LiveData<List<MachineEntity>> getFavoriteMachines();

    @Query("UPDATE machines SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(String id, boolean isFavorite);
}
