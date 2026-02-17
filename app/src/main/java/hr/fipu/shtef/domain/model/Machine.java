package hr.fipu.shtef.domain.model;

import java.util.List;
import hr.fipu.shtef.data.local.entity.PartEntity;

public class Machine {
    private String id;
    private String brand;
    private String model;
    private String ean;
    private float repairabilityScore;
    private String description;
    private String imageUrl;
    private List<PartEntity> parts;

    public Machine() {}

    public Machine(String id, String brand, String model, String ean, float repairabilityScore) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.ean = ean;
        this.repairabilityScore = repairabilityScore;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getEan() { return ean; }
    public void setEan(String ean) { this.ean = ean; }
    public float getRepairabilityScore() { return repairabilityScore; }
    public void setRepairabilityScore(float repairabilityScore) { this.repairabilityScore = repairabilityScore; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<PartEntity> getParts() { return parts; }
    public void setParts(List<PartEntity> parts) { this.parts = parts; }
}
