package hr.fipu.shtef.data.remote;

import java.util.List;

import hr.fipu.shtef.domain.model.Machine;
import hr.fipu.shtef.domain.model.ServiceCenter;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("machines.json")
    Call<List<Machine>> getAllMachines();

    @GET("service_centers.json")
    Call<List<ServiceCenter>> getServiceCenters();
}
