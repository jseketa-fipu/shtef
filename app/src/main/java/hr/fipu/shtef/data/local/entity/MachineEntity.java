package hr.fipu.shtef.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "machines")
public class MachineEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String brand;
    public String model;
    public String ean;
    public float repairabilityScore;
    public String description;
    public String imageUrl;
    public long lastUpdated;
    public boolean isFavorite;

    public MachineEntity() {}
}
