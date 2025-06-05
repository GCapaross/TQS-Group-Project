package nikev.group.project.chargingplatform.DTOs;

public interface StationBasicDTO {
    void setName(String name);
    void setLocation(String location);
    void setLatitude(Double latitude);
    void setLongitude(Double longitude);
    void setPricePerKwh(Double pricePerKwh);
    void setSupportedConnectors(java.util.List<String> supportedConnectors);
    void setCompanyName(String companyName);
    void setWorkerIds(java.util.List<Long> workerIds);
}
