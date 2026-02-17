package hr.fipu.shtef.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "parts",
        foreignKeys = @ForeignKey(entity = MachineEntity.class,
                parentColumns = "id",
                childColumns = "machineId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("machineId")})
public class PartEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String machineId;
    public String name;
    public String category;
    public double price;
    public boolean availability;
    public String supplier;

    public PartEntity() {}
}
