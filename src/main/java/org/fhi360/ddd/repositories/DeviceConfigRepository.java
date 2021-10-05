package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.DeviceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeviceConfigRepository extends JpaRepository<DeviceConfig, Long> {
    DeviceConfig findByDeviceId(String deviceConfig);

    @Query(value = "SELECT count(*) FROM deviceconfig", nativeQuery = true)
    int masDeviceConfigId();

}
